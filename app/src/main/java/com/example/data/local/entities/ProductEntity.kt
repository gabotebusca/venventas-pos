package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String = "",
    val category: String = "General",
    val costPriceUsd: Double,
    val salePriceUsd: Double,
    val stockQuantity: Int = 0,
    val minStockAlert: Int = 5,
    val description: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val profitUsd: Double get() = salePriceUsd - costPriceUsd
    val profitMarginPercent: Double get() = if (costPriceUsd > 0) ((salePriceUsd - costPriceUsd) / costPriceUsd) * 100 else 0.0
}
