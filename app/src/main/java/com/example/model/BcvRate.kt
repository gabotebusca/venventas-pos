package com.example.model

data class BcvRate(
    val usdRate: Double,
    val eurRate: Double = 0.0,
    val lastUpdated: String = "",
    val isWeekend: Boolean = false,
    val isCached: Boolean = false,
    val note: String = "Tasa Oficial del Banco Central de Venezuela"
) {
    companion object {
        // Safe default fallback rate in case of fresh offline start
        val DEFAULT = BcvRate(
            usdRate = 65.45,
            eurRate = 71.20,
            lastUpdated = "Última oficial registrada",
            isWeekend = false,
            isCached = true,
            note = "Tasa BCV de contingencia"
        )
    }
}
