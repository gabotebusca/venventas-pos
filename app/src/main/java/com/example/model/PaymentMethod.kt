package com.example.model

enum class PaymentMethod(
    val displayName: String,
    val requiresReference: Boolean,
    val iconName: String
) {
    PAGO_MOVIL("Pago Móvil", true, "phone_android"),
    TRANSFERENCIA("Transferencia Bancaria", true, "account_balance"),
    CREDITO("Crédito / Fiado", false, "receipt_long"),
    BIOPAGO("BioPago BDV", true, "fingerprint"),
    PUNTO_VENTA("Punto de Venta (Tarjeta)", true, "credit_card"),
    EFECTIVO_USD("Efectivo $ (Dólares)", false, "attach_money"),
    EFECTIVO_BS("Efectivo Bs (Bolívares)", false, "payments"),
    MIXTO("Pago Mixto", true, "swap_horiz")
}

data class CartItem(
    val productId: Long,
    val productName: String,
    val productCode: String,
    val quantity: Int,
    val costPriceUsd: Double,
    val unitPriceUsd: Double,
    val availableStock: Int
) {
    val totalUsd: Double get() = unitPriceUsd * quantity
    val totalCostUsd: Double get() = costPriceUsd * quantity
    val profitUsd: Double get() = totalUsd - totalCostUsd
    
    fun totalBs(bcvRate: Double): Double = totalUsd * bcvRate
}

data class SaleItemRecord(
    val productId: Long,
    val name: String,
    val quantity: Int,
    val unitPriceUsd: Double,
    val totalUsd: Double,
    val costPriceUsd: Double
)
