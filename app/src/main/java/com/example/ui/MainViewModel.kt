package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MerchantPreferences
import com.example.data.MerchantProfile
import com.example.data.local.AppDatabase
import com.example.data.local.entities.CreditAccountEntity
import com.example.data.local.entities.CreditPaymentEntity
import com.example.data.local.entities.ProductEntity
import com.example.data.local.entities.SaleEntity
import com.example.data.repository.PosRepository
import com.example.model.BcvRate
import com.example.model.BiblicalVerse
import com.example.model.CartItem
import com.example.model.PaymentMethod
import com.example.model.SaleItemRecord
import com.example.network.BcvRateService
import com.example.util.CsvExportUtil
import com.example.util.QuincenaManager
import com.example.util.TicketGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val title: String, val iconName: String) {
    POS("Venta", "shopping_cart"),
    INVENTORY("Inventario", "inventory_2"),
    HISTORY("Historial", "receipt_long"),
    CREDITS("Créditos", "account_balance_wallet"),
    CONVERTER("Conversor", "currency_exchange"),
    QUINCENA("Quincena", "notifications_active"),
    ANALYTICS("Ganancias", "insights"),
    SETTINGS("Ajustes", "settings")
}

data class TicketPreviewState(
    val sale: SaleEntity,
    val items: List<SaleItemRecord>,
    val bitmap: Bitmap?,
    val imageUri: Uri?,
    val whatsAppText: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = AppDatabase.getDatabase(context, viewModelScope)
    val repository = PosRepository(database)
    private val bcvService = BcvRateService(context)
    private val merchantPrefs = MerchantPreferences(context)

    // Biblical Splash State
    val biblicalVerse: BiblicalVerse = BiblicalVerse.getRandomVerse()
    private val _isSplashFinished = MutableStateFlow(false)
    val isSplashFinished: StateFlow<Boolean> = _isSplashFinished.asStateFlow()

    private val _splashCountdown = MutableStateFlow(5)
    val splashCountdown: StateFlow<Int> = _splashCountdown.asStateFlow()

    // Navigation & UI State
    private val _currentTab = MutableStateFlow(AppTab.POS)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // BCV Rate
    private val _bcvRate = MutableStateFlow(BcvRate.DEFAULT)
    val bcvRate: StateFlow<BcvRate> = _bcvRate.asStateFlow()

    private val _isRateLoading = MutableStateFlow(false)
    val isRateLoading: StateFlow<Boolean> = _isRateLoading.asStateFlow()

    // Merchant Profile
    private val _merchantProfile = MutableStateFlow(merchantPrefs.getProfile())
    val merchantProfile: StateFlow<MerchantProfile> = _merchantProfile.asStateFlow()

    // Shopping Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Product search and filter
    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery: StateFlow<String> = _productSearchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todos")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Ticket Preview dialog state
    private val _ticketPreview = MutableStateFlow<TicketPreviewState?>(null)
    val ticketPreview: StateFlow<TicketPreviewState?> = _ticketPreview.asStateFlow()

    // UI Message / Toast
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Products Flow
    val allProducts: StateFlow<List<ProductEntity>> = repository.allActiveProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredProducts = combine(allProducts, _productSearchQuery, _selectedCategory) { products, query, category ->
        products.filter { p ->
            val matchesQuery = query.isBlank() || p.name.contains(query, ignoreCase = true) || p.code.contains(query, ignoreCase = true)
            val matchesCategory = category == "Todos" || p.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sales Flow
    val allSales: StateFlow<List<SaleEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Credits Flow
    val allCreditAccounts: StateFlow<List<CreditAccountEntity>> = repository.allCreditAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingDebtors: StateFlow<List<CreditAccountEntity>> = repository.pendingCreditAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        QuincenaManager.createNotificationChannel(context)
        loadBcvRate()
        startSplashTimer()
    }

    private fun startSplashTimer() {
        viewModelScope.launch {
            for (i in 5 downTo 1) {
                _splashCountdown.value = i
                kotlinx.coroutines.delay(1000)
            }
            _splashCountdown.value = 0
            _isSplashFinished.value = true
        }
    }

    fun skipSplash() {
        _isSplashFinished.value = true
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun loadBcvRate() {
        viewModelScope.launch {
            _isRateLoading.value = true
            try {
                val rate = bcvService.getBcvRate()
                _bcvRate.value = rate
            } catch (e: Exception) {
                _bcvRate.value = bcvService.getSavedRate()
            } finally {
                _isRateLoading.value = false
            }
        }
    }

    fun setCustomBcvRate(customRate: Double) {
        val updated = bcvService.updateManualRate(customRate)
        _bcvRate.value = updated
        _userMessage.value = "Tasa BCV actualizada a Bs. ${customRate}"
    }

    fun setProductSearch(query: String) {
        _productSearchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    // Cart Operations
    fun addToCart(product: ProductEntity) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.productId == product.id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            current.add(
                CartItem(
                    productId = product.id,
                    productName = product.name,
                    productCode = product.code,
                    quantity = 1,
                    costPriceUsd = product.costPriceUsd,
                    unitPriceUsd = product.salePriceUsd,
                    availableStock = product.stockQuantity
                )
            )
        }
        _cartItems.value = current
    }

    fun updateCartQuantity(productId: Long, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(productId)
            return
        }
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = newQuantity)
            _cartItems.value = current
        }
    }

    fun removeFromCart(productId: Long) {
        _cartItems.value = _cartItems.value.filter { it.productId != productId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // Process Sale
    fun processSale(
        customerName: String,
        customerPhone: String,
        customerCedula: String,
        paymentMethod: PaymentMethod,
        paymentReference: String,
        notes: String
    ) {
        val items = _cartItems.value
        if (items.isEmpty()) {
            _userMessage.value = "El carrito está vacío"
            return
        }

        viewModelScope.launch {
            try {
                val currentRate = _bcvRate.value.usdRate
                val sale = repository.processSale(
                    customerName = customerName,
                    customerPhone = customerPhone,
                    customerCedula = customerCedula,
                    cartItems = items,
                    bcvRate = currentRate,
                    paymentMethod = paymentMethod,
                    paymentReference = paymentReference,
                    notes = notes
                )

                val saleItemRecords = items.map {
                    SaleItemRecord(
                        productId = it.productId,
                        name = it.productName,
                        quantity = it.quantity,
                        unitPriceUsd = it.unitPriceUsd,
                        totalUsd = it.totalUsd,
                        costPriceUsd = it.costPriceUsd
                    )
                }

                // Generate Ticket Bitmap & Uri
                val profile = _merchantProfile.value
                val bitmap = TicketGenerator.createTicketBitmap(context, sale, saleItemRecords, profile)
                val uri = TicketGenerator.saveTicketImage(context, bitmap, "Ticket_${sale.invoiceNumber}")
                val whatsAppText = TicketGenerator.buildWhatsAppTextMessage(sale, saleItemRecords, profile)

                _ticketPreview.value = TicketPreviewState(
                    sale = sale,
                    items = saleItemRecords,
                    bitmap = bitmap,
                    imageUri = uri,
                    whatsAppText = whatsAppText
                )

                clearCart()
                _userMessage.value = "¡Venta ${sale.invoiceNumber} procesada con éxito!"
            } catch (e: Exception) {
                _userMessage.value = "Error al procesar la venta: ${e.message}"
            }
        }
    }

    fun closeTicketPreview() {
        _ticketPreview.value = null
    }

    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.saveProduct(product)
            _userMessage.value = "Producto guardado"
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _userMessage.value = "Producto eliminado"
        }
    }

    fun recordCreditPayment(
        account: CreditAccountEntity,
        amountUsd: Double,
        paymentMethod: PaymentMethod,
        reference: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.recordCreditPayment(
                account = account,
                amountUsd = amountUsd,
                bcvRate = _bcvRate.value.usdRate,
                paymentMethod = paymentMethod,
                reference = reference,
                notes = notes
            )
            _userMessage.value = "Abono de $${amountUsd} registrado para ${account.customerName}"
        }
    }

    fun saveMerchantProfile(profile: MerchantProfile) {
        merchantPrefs.saveProfile(profile)
        _merchantProfile.value = profile
        _userMessage.value = "Datos del negocio actualizados"
    }

    fun exportSalesCsv() {
        val list = allSales.value
        val uri = CsvExportUtil.exportSalesToCsv(context, list)
        if (uri != null) {
            CsvExportUtil.shareCsvFile(context, uri, "Reporte de Ventas")
        } else {
            _userMessage.value = "Error al generar archivo CSV"
        }
    }

    fun exportProductsCsv() {
        val list = allProducts.value
        val uri = CsvExportUtil.exportProductsToCsv(context, list)
        if (uri != null) {
            CsvExportUtil.shareCsvFile(context, uri, "Inventario de Productos")
        } else {
            _userMessage.value = "Error al generar inventario CSV"
        }
    }

    fun exportCreditsCsv() {
        val list = allCreditAccounts.value
        val uri = CsvExportUtil.exportCreditsToCsv(context, list)
        if (uri != null) {
            CsvExportUtil.shareCsvFile(context, uri, "Cuentas por Cobrar")
        } else {
            _userMessage.value = "Error al generar reporte de créditos"
        }
    }

    fun sendQuincenaNotificationNow() {
        val debtors = pendingDebtors.value
        val totalUsd = debtors.sumOf { it.balancePendingUsd }
        QuincenaManager.showQuincenaNotification(context, debtors.size, totalUsd)
        _userMessage.value = "Notificación de quincena enviada"
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
