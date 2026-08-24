package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_accounts")
data class CreditAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerName: String,
    val customerPhone: String = "",
    val customerCedula: String = "",
    val totalDebtUsd: Double = 0.0,
    val totalPaidUsd: Double = 0.0,
    val balancePendingUsd: Double = 0.0,
    val lastPurchaseTimestamp: Long = System.currentTimeMillis(),
    val nextQuincenaDueDate: String = "15 de Quincena",
    val notes: String = ""
)

@Entity(tableName = "credit_payments")
data class CreditPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val creditAccountId: Long,
    val customerName: String,
    val amountUsd: Double,
    val amountBs: Double,
    val bcvRate: Double,
    val paymentMethod: String,
    val paymentReference: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
