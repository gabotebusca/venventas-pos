package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.MerchantProfile
import com.example.data.local.entities.SaleEntity
import com.example.model.SaleItemRecord
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TicketGenerator {

    private val currencyUsdFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val numberBsFormat = NumberFormat.getNumberInstance(Locale("es", "VE")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    fun formatUsd(amount: Double): String = currencyUsdFormat.format(amount)
    fun formatBs(amount: Double): String = "Bs. " + numberBsFormat.format(amount)

    /**
     * Generates a high-quality receipt bitmap image
     */
    fun createTicketBitmap(
        context: Context,
        sale: SaleEntity,
        items: List<SaleItemRecord>,
        merchant: MerchantProfile
    ): Bitmap {
        val width = 720
        // Calculate dynamic height based on item count
        val baseHeight = 1150
        val itemHeight = 70
        val height = baseHeight + (items.size * itemHeight)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textBounds = Rect()

        // Top decorative bar (Venezuelan Tricolor inspiration: Gold, Blue, Red stripes)
        paint.color = Color.parseColor("#FFCC00") // Yellow
        canvas.drawRect(0f, 0f, width.toFloat(), 12f, paint)
        paint.color = Color.parseColor("#00247D") // Blue
        canvas.drawRect(0f, 12f, width.toFloat(), 20f, paint)
        paint.color = Color.parseColor("#CF142B") // Red
        canvas.drawRect(0f, 20f, width.toFloat(), 26f, paint)

        var y = 80f

        // Business Header
        paint.color = Color.parseColor("#0F2A4A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 38f
        drawCenteredText(canvas, merchant.businessName, width / 2f, y, paint)

        y += 44f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 24f
        paint.color = Color.parseColor("#475569")
        drawCenteredText(canvas, "RIF: ${merchant.rif}", width / 2f, y, paint)

        y += 34f
        drawCenteredText(canvas, "Pago Móvil: ${merchant.phonePagoMovil} (${merchant.bankName})", width / 2f, y, paint)

        y += 45f
        drawDottedLine(canvas, 30f, width - 30f, y, paint)

        y += 45f
        // Receipt info
        paint.textSize = 26f
        paint.color = Color.parseColor("#1E293B")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("COMPROBANTE DE VENTA: ${sale.invoiceNumber}", 40f, y, paint)

        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        val dateStr = sdf.format(Date(sale.timestamp))
        y += 36f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 23f
        canvas.drawText("Fecha: $dateStr", 40f, y, paint)

        y += 36f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Cliente: ${sale.customerName}", 40f, y, paint)

        if (sale.customerCedula.isNotEmpty() || sale.customerPhone.isNotEmpty()) {
            y += 32f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val info = listOfNotNull(
                if (sale.customerCedula.isNotEmpty()) "C.I: ${sale.customerCedula}" else null,
                if (sale.customerPhone.isNotEmpty()) "Tlf: ${sale.customerPhone}" else null
            ).joinToString(" | ")
            canvas.drawText(info, 40f, y, paint)
        }

        y += 40f
        drawDottedLine(canvas, 30f, width - 30f, y, paint)

        // Column Headers
        y += 38f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        paint.color = Color.parseColor("#0F2A4A")
        canvas.drawText("Cant. Descripción", 40f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Total ($ / Bs)", width - 40f, y, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 20f
        paint.strokeWidth = 2f
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawLine(40f, y, width - 40f, y, paint)

        // Line Items
        y += 36f
        for (item in items) {
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 24f
            paint.color = Color.parseColor("#1E293B")

            val nameTruncated = if (item.name.length > 25) item.name.take(24) + "…" else item.name
            canvas.drawText("${item.quantity}x  $nameTruncated", 40f, y, paint)

            paint.textAlign = Paint.Align.RIGHT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val itemTotalUsd = formatUsd(item.totalUsd)
            canvas.drawText(itemTotalUsd, width - 40f, y, paint)

            y += 28f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 20f
            paint.color = Color.parseColor("#64748B")
            val itemTotalBs = formatBs(item.totalUsd * sale.bcvRate)
            canvas.drawText(itemTotalBs, width - 40f, y, paint)

            paint.textAlign = Paint.Align.LEFT
            y += 38f
        }

        y += 10f
        drawDottedLine(canvas, 30f, width - 30f, y, paint)

        // Summary Totals
        y += 45f
        paint.color = Color.parseColor("#475569")
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Tasa Oficial BCV:", 40f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(formatBs(sale.bcvRate) + " / $", width - 40f, y, paint)

        paint.textAlign = Paint.Align.LEFT
        y += 40f
        paint.color = Color.parseColor("#0F2A4A")
        paint.textSize = 26f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL EN DÓLARES:", 40f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.parseColor("#0B6E4F") // Emerald green
        paint.textSize = 30f
        canvas.drawText(formatUsd(sale.totalUsd), width - 40f, y, paint)

        paint.textAlign = Paint.Align.LEFT
        y += 42f
        paint.color = Color.parseColor("#0F2A4A")
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL EN BOLÍVARES (Bs):", 40f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.parseColor("#0F2A4A")
        paint.textSize = 32f
        canvas.drawText(formatBs(sale.totalBs), width - 40f, y, paint)

        paint.textAlign = Paint.Align.LEFT
        y += 44f
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#334155")
        canvas.drawText("Método de Pago: ${sale.paymentMethod}", 40f, y, paint)

        if (sale.paymentReference.isNotEmpty()) {
            y += 30f
            canvas.drawText("Ref. / Comprobante: ${sale.paymentReference}", 40f, y, paint)
        }

        y += 45f
        drawDottedLine(canvas, 30f, width - 30f, y, paint)

        // Footer Blessing and Thank You
        y += 50f
        paint.color = Color.parseColor("#0F2A4A")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        drawCenteredText(canvas, merchant.thankYouMessage, width / 2f, y, paint)

        y += 34f
        paint.color = Color.parseColor("#64748B")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        paint.textSize = 20f
        drawCenteredText(canvas, "«Pon en manos del Señor todas tus obras» Proverbios 16:3", width / 2f, y, paint)

        y += 30f
        drawCenteredText(canvas, "Emitido con VenVentas POS • Tasa Oficial BCV", width / 2f, y, paint)

        return bitmap
    }

    private fun drawCenteredText(canvas: Canvas, text: String, cx: Float, y: Float, paint: Paint) {
        val oldAlign = paint.textAlign
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(text, cx, y, paint)
        paint.textAlign = oldAlign
    }

    private fun drawDottedLine(canvas: Canvas, startX: Float, endX: Float, y: Float, paint: Paint) {
        val dottedPaint = Paint(paint).apply {
            color = Color.parseColor("#94A3B8")
            strokeWidth = 2f
            pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
        }
        canvas.drawLine(startX, y, endX, y, dottedPaint)
    }

    /**
     * Saves bitmap to cache file and returns content URI for FileProvider
     */
    fun saveTicketImage(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "tickets")
            cachePath.mkdirs()
            val file = File(cachePath, "$fileName.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Creates formatted text for WhatsApp message (simple sale entity overload)
     */
    fun buildWhatsAppSaleText(
        sale: SaleEntity,
        merchant: MerchantProfile
    ): String {
        return buildWhatsAppTextMessage(sale, emptyList(), merchant)
    }

    /**
     * Creates formatted text for WhatsApp message
     */
    fun buildWhatsAppTextMessage(
        sale: SaleEntity,
        items: List<SaleItemRecord>,
        merchant: MerchantProfile
    ): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        val dateStr = sdf.format(Date(sale.timestamp))

        val itemsList = items.joinToString("\n") { item ->
            "• ${item.quantity}x ${item.name} = ${formatUsd(item.totalUsd)} (${formatBs(item.totalUsd * sale.bcvRate)})"
        }

        return """
🧾 *COMPROBANTE DE VENTA*
🏢 *${merchant.businessName}*
📌 RIF: ${merchant.rif}
📅 Fecha: $dateStr
🔢 N° Factura: *${sale.invoiceNumber}*
👤 Cliente: *${sale.customerName}*

🛒 *Detalle de Compra:*
$itemsList

━━━━━━━━━━━━━━━━━━━━
💵 *Total Dólares:* *${formatUsd(sale.totalUsd)}*
🇻🇪 *Total Bolívares (BCV):* *${formatBs(sale.totalBs)}*
📊 *Tasa BCV:* ${formatBs(sale.bcvRate)} / $
💳 *Método de Pago:* ${sale.paymentMethod}
${if (sale.paymentReference.isNotEmpty()) "🔖 *Referencia:* ${sale.paymentReference}\n" else ""}━━━━━━━━━━━━━━━━━━━━
🙏 _${merchant.thankYouMessage}_
✨ _«Pon en manos del Señor tus obras» Proverbios 16:3_
""".trimIndent()
    }

    /**
     * Share ticket image directly to WhatsApp or general share sheet
     */
    fun shareTicket(context: Context, imageUri: Uri, messageText: String, phoneNumber: String = "") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, messageText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (phoneNumber.isNotBlank()) {
            // Clean phone for WhatsApp international format (Venezuela +58)
            val cleanPhone = formatVenezuelaPhone(phoneNumber)
            intent.putExtra("jid", "$cleanPhone@s.whatsapp.net")
        }

        val chooser = Intent.createChooser(intent, "Compartir Ticket por WhatsApp")
        context.startActivity(chooser)
    }

    /**
     * Direct WhatsApp link
     */
    fun getWhatsAppDirectUrl(phoneNumber: String, messageText: String): String {
        val cleanPhone = formatVenezuelaPhone(phoneNumber)
        val encodedText = URLEncoder.encode(messageText, "UTF-8")
        return if (cleanPhone.isNotEmpty()) {
            "https://wa.me/$cleanPhone?text=$encodedText"
        } else {
            "https://api.whatsapp.com/send?text=$encodedText"
        }
    }

    fun formatVenezuelaPhone(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.startsWith("58") && digits.length >= 12 -> digits
            digits.startsWith("04") || digits.startsWith("02") -> "58" + digits.substring(1)
            digits.length == 10 && (digits.startsWith("4") || digits.startsWith("2")) -> "58$digits"
            else -> digits
        }
    }
}
