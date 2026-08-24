package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.CreditAccountEntity
import com.example.data.local.entities.CreditPaymentEntity
import com.example.data.local.entities.ProductEntity
import com.example.data.local.entities.SaleEntity
import com.example.model.CartItem
import com.example.model.PaymentMethod
import com.example.model.SaleItemRecord
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PosRepository(private val database: AppDatabase) {

    private val productDao = database.productDao()
    private val saleDao = database.saleDao()
    private val creditDao = database.creditDao()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, SaleItemRecord::class.java)
    private val jsonAdapter = moshi.adapter<List<SaleItemRecord>>(listType)

    val allActiveProducts: Flow<List<ProductEntity>> = productDao.getAllActiveProducts()
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
    val allCreditAccounts: Flow<List<CreditAccountEntity>> = creditDao.getAllCreditAccounts()
    val pendingCreditAccounts: Flow<List<CreditAccountEntity>> = creditDao.getPendingCreditAccounts()

    suspend fun saveProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        if (product.id == 0L) {
            productDao.insertProduct(product)
        } else {
            productDao.updateProduct(product)
        }
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    suspend fun processSale(
        customerName: String,
        customerPhone: String,
        customerCedula: String,
        cartItems: List<CartItem>,
        bcvRate: Double,
        paymentMethod: PaymentMethod,
        paymentReference: String,
        notes: String = ""
    ): SaleEntity = withContext(Dispatchers.IO) {
        val totalUsd = cartItems.sumOf { it.totalUsd }
        val totalBs = totalUsd * bcvRate
        val totalCostUsd = cartItems.sumOf { it.totalCostUsd }
        val profitUsd = totalUsd - totalCostUsd

        val count = saleDao.getSalesCount()
        val invoiceNumber = String.format(Locale.US, "VEN-%05d", count + 1)

        val itemRecords = cartItems.map {
            SaleItemRecord(
                productId = it.productId,
                name = it.productName,
                quantity = it.quantity,
                unitPriceUsd = it.unitPriceUsd,
                totalUsd = it.totalUsd,
                costPriceUsd = it.costPriceUsd
            )
        }

        val itemsJson = jsonAdapter.toJson(itemRecords)

        val isCredit = (paymentMethod == PaymentMethod.CREDITO)
        val paymentStatus = if (isCredit) "CREDITO_PENDIENTE" else "PAGADO"

        val sale = SaleEntity(
            invoiceNumber = invoiceNumber,
            customerName = customerName.ifBlank { "Cliente General" },
            customerPhone = customerPhone,
            customerCedula = customerCedula,
            timestamp = System.currentTimeMillis(),
            bcvRate = bcvRate,
            totalUsd = totalUsd,
            totalBs = totalBs,
            totalCostUsd = totalCostUsd,
            profitUsd = profitUsd,
            paymentMethod = paymentMethod.displayName,
            paymentReference = paymentReference,
            paymentStatus = paymentStatus,
            itemsJson = itemsJson,
            notes = notes
        )

        val saleId = saleDao.insertSale(sale)

        // Reduce stock for each product sold
        for (item in cartItems) {
            productDao.reduceStock(item.productId, item.quantity)
        }

        // If sale was on credit, update or create CreditAccount
        if (isCredit) {
            val existingAccount = creditDao.getCreditAccountByName(customerName.trim())
            if (existingAccount != null) {
                val updated = existingAccount.copy(
                    totalDebtUsd = existingAccount.totalDebtUsd + totalUsd,
                    balancePendingUsd = existingAccount.balancePendingUsd + totalUsd,
                    lastPurchaseTimestamp = System.currentTimeMillis(),
                    customerPhone = if (customerPhone.isNotEmpty()) customerPhone else existingAccount.customerPhone,
                    customerCedula = if (customerCedula.isNotEmpty()) customerCedula else existingAccount.customerCedula
                )
                creditDao.updateAccount(updated)
            } else {
                val newAccount = CreditAccountEntity(
                    customerName = customerName.trim(),
                    customerPhone = customerPhone,
                    customerCedula = customerCedula,
                    totalDebtUsd = totalUsd,
                    totalPaidUsd = 0.0,
                    balancePendingUsd = totalUsd,
                    lastPurchaseTimestamp = System.currentTimeMillis(),
                    nextQuincenaDueDate = getNextQuincenaDateString()
                )
                creditDao.insertOrUpdateAccount(newAccount)
            }
        }

        return@withContext sale.copy(id = saleId)
    }

    suspend fun recordCreditPayment(
        account: CreditAccountEntity,
        amountUsd: Double,
        bcvRate: Double,
        paymentMethod: PaymentMethod,
        reference: String,
        notes: String
    ) = withContext(Dispatchers.IO) {
        val payment = CreditPaymentEntity(
            creditAccountId = account.id,
            customerName = account.customerName,
            amountUsd = amountUsd,
            amountBs = amountUsd * bcvRate,
            bcvRate = bcvRate,
            paymentMethod = paymentMethod.displayName,
            paymentReference = reference,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )

        creditDao.insertPayment(payment)

        val newPaid = account.totalPaidUsd + amountUsd
        val newBalance = (account.balancePendingUsd - amountUsd).coerceAtLeast(0.0)

        val updatedAccount = account.copy(
            totalPaidUsd = newPaid,
            balancePendingUsd = newBalance
        )
        creditDao.updateAccount(updatedAccount)
    }

    fun getPaymentsForAccount(accountId: Long): Flow<List<CreditPaymentEntity>> {
        return creditDao.getPaymentsForAccount(accountId)
    }

    fun parseSaleItems(json: String): List<SaleItemRecord> {
        return try {
            jsonAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getNextQuincenaDateString(): String {
        val cal = java.util.Calendar.getInstance()
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val monthFormat = SimpleDateFormat("MMMM", Locale("es", "VE"))
        val monthName = monthFormat.format(cal.time)
        return if (day <= 15) {
            "15 de $monthName"
        } else {
            "30 de $monthName"
        }
    }
}
