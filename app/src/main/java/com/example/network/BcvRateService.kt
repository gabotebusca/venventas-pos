package com.example.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.model.BcvRate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class BcvRateService(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("bcv_rate_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    suspend fun getBcvRate(): BcvRate = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)

        // Try API 1: ve.dolarapi.com
        try {
            val request = Request.Builder()
                .url("https://ve.dolarapi.com/v1/dolares/oficial")
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrEmpty()) {
                    val json = JSONObject(body)
                    val rate = json.optDouble("promedio", json.optDouble("precio", 0.0))
                    val fechaActualizacion = json.optString("fechaActualizacion", "")

                    if (rate > 0.0) {
                        val formattedDate = formatDateString(fechaActualizacion)
                        val bcv = BcvRate(
                            usdRate = rate,
                            lastUpdated = formattedDate,
                            isWeekend = isWeekend,
                            isCached = false,
                            note = if (isWeekend) "Tasa BCV de cierre semanal (Fin de Semana)" else "Tasa Oficial BCV en vivo"
                        )
                        saveLastRate(bcv)
                        return@withContext bcv
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("BcvRateService", "Primary BCV API failed, trying fallback: ${e.message}")
        }

        // Try API 2: pydolarve.org
        try {
            val request2 = Request.Builder()
                .url("https://pydolarve.org/api/v1/dollar?page=bcv")
                .header("Accept", "application/json")
                .build()

            val response2 = client.newCall(request2).execute()
            if (response2.isSuccessful) {
                val body = response2.body?.string()
                if (!body.isNullOrEmpty()) {
                    val json = JSONObject(body)
                    val monitors = json.optJSONObject("monitors")
                    val usdObj = monitors?.optJSONObject("usd")
                    val price = usdObj?.optDouble("price", 0.0) ?: 0.0
                    val lastUpdate = usdObj?.optString("last_update", "") ?: ""

                    if (price > 0.0) {
                        val bcv = BcvRate(
                            usdRate = price,
                            lastUpdated = if (lastUpdate.isNotEmpty()) lastUpdate else getCurrentDateFormatted(),
                            isWeekend = isWeekend,
                            isCached = false,
                            note = if (isWeekend) "Tasa BCV de cierre semanal (Fin de Semana)" else "Tasa Oficial BCV en vivo"
                        )
                        saveLastRate(bcv)
                        return@withContext bcv
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("BcvRateService", "Fallback BCV API failed: ${e.message}")
        }

        // Fallback to local stored rate or safe default
        return@withContext getSavedRate(isWeekend)
    }

    fun saveLastRate(bcvRate: BcvRate) {
        prefs.edit()
            .putFloat("last_usd_rate", bcvRate.usdRate.toFloat())
            .putString("last_updated", bcvRate.lastUpdated)
            .putLong("last_saved_timestamp", System.currentTimeMillis())
            .apply()
    }

    fun updateManualRate(customRate: Double): BcvRate {
        val bcv = BcvRate(
            usdRate = customRate,
            lastUpdated = "${getCurrentDateFormatted()} (Manual)",
            isWeekend = false,
            isCached = false,
            note = "Tasa personalizada establecida por el usuario"
        )
        saveLastRate(bcv)
        return bcv
    }

    fun getSavedRate(isWeekend: Boolean = false): BcvRate {
        val savedRate = prefs.getFloat("last_usd_rate", 66.85f).toDouble()
        val savedUpdated = prefs.getString("last_updated", "Guardada en el teléfono") ?: "Guardada"
        
        return BcvRate(
            usdRate = savedRate,
            lastUpdated = savedUpdated,
            isWeekend = isWeekend,
            isCached = true,
            note = if (isWeekend) "Tasa BCV de cierre guardada (Fin de semana)" else "Tasa BCV guardada en memoria"
        )
    }

    private fun formatDateString(raw: String): String {
        return try {
            if (raw.isEmpty()) return getCurrentDateFormatted()
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(raw.substringBefore("."))
            if (date != null) outputFormat.format(date) else getCurrentDateFormatted()
        } catch (e: Exception) {
            getCurrentDateFormatted()
        }
    }

    private fun getCurrentDateFormatted(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }
}
