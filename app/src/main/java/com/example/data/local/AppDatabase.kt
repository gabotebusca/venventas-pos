package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CreditDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SaleDao
import com.example.data.local.entities.CreditAccountEntity
import com.example.data.local.entities.CreditPaymentEntity
import com.example.data.local.entities.ProductEntity
import com.example.data.local.entities.SaleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        SaleEntity::class,
        CreditAccountEntity::class,
        CreditPaymentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun creditDao(): CreditDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ven_ventas_pos.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialProducts(database.productDao())
                    }
                }
            }

            suspend fun populateInitialProducts(productDao: ProductDao) {
                val initialProducts = listOf(
                    ProductEntity(name = "Harina PAN Tradicional 1kg", code = "7591010001", category = "Víveres", costPriceUsd = 0.95, salePriceUsd = 1.30, stockQuantity = 48, minStockAlert = 10, description = "Harina de maíz blanco precocida"),
                    ProductEntity(name = "Arroz Mary Tradicional 1kg", code = "7591020002", category = "Víveres", costPriceUsd = 1.10, salePriceUsd = 1.45, stockQuantity = 36, minStockAlert = 8, description = "Arroz blanco tipo I"),
                    ProductEntity(name = "Aceite de Maíz Mazeite 1L", code = "7591030003", category = "Víveres", costPriceUsd = 2.80, salePriceUsd = 3.60, stockQuantity = 24, minStockAlert = 6, description = "Aceite vegetal 100% puro"),
                    ProductEntity(name = "Pasta Primor Corta 1kg", code = "7591040004", category = "Víveres", costPriceUsd = 1.20, salePriceUsd = 1.65, stockQuantity = 30, minStockAlert = 8, description = "Pasta de sémola de trigo"),
                    ProductEntity(name = "Café Fama de América 250g", code = "7591050005", category = "Café y Bebidas", costPriceUsd = 1.80, salePriceUsd = 2.40, stockQuantity = 20, minStockAlert = 5, description = "Café molido gourmet"),
                    ProductEntity(name = "Leche Completa La Campiña 900g", code = "7591060006", category = "Lácteos", costPriceUsd = 7.20, salePriceUsd = 9.00, stockQuantity = 15, minStockAlert = 4, description = "Leche en polvo entera"),
                    ProductEntity(name = "Queso Llanero Blanco (kg)", code = "7591070007", category = "Charcutería", costPriceUsd = 3.80, salePriceUsd = 5.20, stockQuantity = 18, minStockAlert = 5, description = "Queso duro rallar fresco"),
                    ProductEntity(name = "Mantequilla Mavesa 500g", code = "7591080008", category = "Víveres", costPriceUsd = 2.20, salePriceUsd = 2.90, stockQuantity = 25, minStockAlert = 6, description = "Margarina con sal"),
                    ProductEntity(name = "Azúcar Montalbán 1kg", code = "7591090009", category = "Víveres", costPriceUsd = 1.15, salePriceUsd = 1.50, stockQuantity = 40, minStockAlert = 10, description = "Azúcar refinada nacional"),
                    ProductEntity(name = "Refresco Coca-Cola 1.5L", code = "7591100010", category = "Bebidas", costPriceUsd = 1.40, salePriceUsd = 1.95, stockQuantity = 24, minStockAlert = 6, description = "Bebida gaseosa")
                )
                productDao.insertAll(initialProducts)
            }
        }
    }
}
