package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.CreditAccountEntity
import com.example.data.local.entities.CreditPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditDao {
    @Query("SELECT * FROM credit_accounts ORDER BY balancePendingUsd DESC")
    fun getAllCreditAccounts(): Flow<List<CreditAccountEntity>>

    @Query("SELECT * FROM credit_accounts WHERE balancePendingUsd > 0.01 ORDER BY balancePendingUsd DESC")
    fun getPendingCreditAccounts(): Flow<List<CreditAccountEntity>>

    @Query("SELECT * FROM credit_accounts WHERE id = :id LIMIT 1")
    suspend fun getCreditAccountById(id: Long): CreditAccountEntity?

    @Query("SELECT * FROM credit_accounts WHERE customerName = :name LIMIT 1")
    suspend fun getCreditAccountByName(name: String): CreditAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAccount(account: CreditAccountEntity): Long

    @Update
    suspend fun updateAccount(account: CreditAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: CreditPaymentEntity): Long

    @Query("SELECT * FROM credit_payments WHERE creditAccountId = :accountId ORDER BY timestamp DESC")
    fun getPaymentsForAccount(accountId: Long): Flow<List<CreditPaymentEntity>>

    @Query("SELECT * FROM credit_payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<CreditPaymentEntity>>

    @Query("DELETE FROM credit_accounts WHERE id = :id")
    suspend fun deleteAccount(id: Long)
}
