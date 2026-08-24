package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.MerchantProfile
import com.example.data.local.entities.CreditAccountEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object QuincenaManager {

    const val CHANNEL_ID = "quincena_reminders_channel"
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de Cobro (Días 15 y 30)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones automáticas en fechas de quincena venezolana para cobrar cuentas pendientes."
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Checks if today is quincena (14, 15, 29, 30, 31)
     */
    fun isQuincenaPeriod(): Boolean {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val maxDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return day in 14..15 || day >= (maxDayOfMonth - 1)
    }

    fun getQuincenaStatusText(): String {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return when {
            day in 14..15 -> "🔔 ¡Hoy es Quincena (Día $day)! Momento ideal para cobrar por WhatsApp."
            day >= (maxDay - 1) -> "🔔 ¡Fin de Mes / Quincena (Día $day)! Momento ideal para cobrar por WhatsApp."
            day < 14 -> "Próximo corte de quincena: 15 de ${getCurrentMonthName()}"
            else -> "Próximo corte de quincena: 30 de ${getCurrentMonthName()}"
        }
    }

    fun getCurrentMonthName(): String {
        val sdf = SimpleDateFormat("MMMM", Locale("es", "VE"))
        return sdf.format(Date()).replaceFirstChar { it.uppercase() }
    }

    /**
     * Builds WhatsApp collection message
     */
    fun buildCollectionWhatsAppMessage(
        debtor: CreditAccountEntity,
        bcvRate: Double,
        merchant: MerchantProfile
    ): String {
        val balanceBs = debtor.balancePendingUsd * bcvRate
        return """
👋 *Hola, ${debtor.customerName}*!
Recibe un cordial saludo de parte de *${merchant.businessName}*.

🗓️ En esta quincena te recordamos con aprecio tu saldo pendiente en cuenta:
💵 *Saldo Pendiente:* *${TicketGenerator.formatUsd(debtor.balancePendingUsd)}*
🇻🇪 *Equivalente en Bolívares:* *${TicketGenerator.formatBs(balanceBs)}* (Tasa Oficial BCV: ${TicketGenerator.formatBs(bcvRate)})

📲 *Datos para Pago Móvil:*
• Teléfono: *${merchant.phonePagoMovil}*
• Banco: *${merchant.bankName}*
• C.I. / RIF: *${merchant.cedulaOwner}*

Al realizar tu pago, por favor envíanos la captura o número de referencia por este chat para registrarlo inmediatamente.

¡Muchísimas gracias por tu confianza y preferencia! 🙏✨
""".trimIndent()
    }

    /**
     * Opens WhatsApp directly with customer chat
     */
    fun sendWhatsAppReminder(
        context: Context,
        debtor: CreditAccountEntity,
        bcvRate: Double,
        merchant: MerchantProfile
    ) {
        val message = buildCollectionWhatsAppMessage(debtor, bcvRate, merchant)
        val cleanPhone = TicketGenerator.formatVenezuelaPhone(debtor.customerPhone)
        val encodedText = URLEncoder.encode(message, "UTF-8")

        val uri = if (cleanPhone.isNotEmpty()) {
            Uri.parse("https://wa.me/$cleanPhone?text=$encodedText")
        } else {
            Uri.parse("https://api.whatsapp.com/send?text=$encodedText")
        }

        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Enviar recordatorio"))
        }
    }

    /**
     * External API Integration (e.g. CallMeBot / Custom Webhook Gateway)
     */
    suspend fun sendAutomatedExternalApiReminder(
        debtor: CreditAccountEntity,
        bcvRate: Double,
        merchant: MerchantProfile
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val message = buildCollectionWhatsAppMessage(debtor, bcvRate, merchant)
            val cleanPhone = TicketGenerator.formatVenezuelaPhone(debtor.customerPhone)
            val encodedMsg = URLEncoder.encode(message, "UTF-8")

            // Format webhook url: allows callmebot or custom user api
            val url = if (merchant.webhookApiUrl.contains("callmebot")) {
                "${merchant.webhookApiUrl}?phone=$cleanPhone&text=$encodedMsg"
            } else {
                "${merchant.webhookApiUrl}?phone=$cleanPhone&message=$encodedMsg&amountUsd=${debtor.balancePendingUsd}&amountBs=${debtor.balancePendingUsd * bcvRate}"
            }

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success("Recordatorio enviado con éxito vía API Externa")
            } else {
                Result.failure(Exception("Error en API externa (Código: ${response.code})"))
            }
        } catch (e: Exception) {
            Log.e("QuincenaManager", "External API reminder error", e)
            Result.failure(e)
        }
    }

    /**
     * Show Quincena Alert Notification
     */
    fun showQuincenaNotification(context: Context, totalDebtors: Int, totalPendingUsd: Double) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1530,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💵 ¡Alerta de Quincena (15 / 30)!")
            .setContentText("Tienes $totalDebtors clientes por cobrar (${TicketGenerator.formatUsd(totalPendingUsd)}). ¡Envía tus recordatorios!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Hoy es quincena en Venezuela. Tienes $totalDebtors cuentas por cobrar por un total de ${TicketGenerator.formatUsd(totalPendingUsd)}. Abre la app para enviar recordatorios automáticos por WhatsApp con un solo toque.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1530, notification)
    }
}
