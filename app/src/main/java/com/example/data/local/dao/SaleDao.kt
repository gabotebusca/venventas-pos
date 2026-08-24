package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getSaleById(id: Long): SaleEntity?

    @Query("SELECT * FROM sales WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp ORDER BY timestamp DESC")
    fun getSalesBetweenDates(startTimestamp: Long, endTimestamp: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE customerName LIKE '%' || :query || '%' OR invoiceNumber LIKE '%' || :query || '%'")
    fun searchSales(query: String): Flow<List<SaleEntity>>

    @Query("SELECT COUNT(*) FROM sales")
    suspend fun getSalesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSale(id: Long)

    @Query("SELECT SUM(totalUsd) FROM sales")
    fun getTotalSalesUsd(): Flow<Double?>

    @Query("SELECT SUM(profitUsd) FROM sales")
    fun getTotalProfitUsd(): Flow<Double?>
}
