package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val customerName: String,
    val customerPhone: String = "",
    val customerCedula: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val bcvRate: Double,
    val totalUsd: Double,
    val totalBs: Double,
    val totalCostUsd: Double,
    val profitUsd: Double,
    val paymentMethod: String,
    val paymentReference: String = "",
    val paymentStatus: String = "PAGADO", // PAGADO, CREDITO_PENDIENTE, ABONADO
    val itemsJson: String, // Stored as simple structured JSON or line items
    val notes: String = ""
)
