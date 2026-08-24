package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.entities.CreditAccountEntity
import com.example.data.local.entities.ProductEntity
import com.example.data.local.entities.SaleEntity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExportUtil {

    private val sdf = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault())
    private val dateDisplay = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun exportSalesToCsv(context: Context, sales: List<SaleEntity>): Uri? {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            exportDir.mkdirs()
            val fileName = "Ventas_VenVentas_${sdf.format(Date())}.csv"
            val file = File(exportDir, fileName)

            val writer = FileWriter(file)
            // UTF-8 BOM for Excel compatibility
            writer.write("\uFEFF")
            writer.write("Factura,Fecha,Cliente,Cedula,Telefono,Tasa BCV,Total USD,Total Bs,Costo USD,Ganancia USD,Metodo Pago,Referencia,Estado,Notas\n")

            for (sale in sales) {
                val line = buildCsvRow(
                    sale.invoiceNumber,
                    dateDisplay.format(Date(sale.timestamp)),
                    sale.customerName,
                    sale.customerCedula,
                    sale.customerPhone,
                    String.format(Locale.US, "%.2f", sale.bcvRate),
                    String.format(Locale.US, "%.2f", sale.totalUsd),
                    String.format(Locale.US, "%.2f", sale.totalBs),
                    String.format(Locale.US, "%.2f", sale.totalCostUsd),
                    String.format(Locale.US, "%.2f", sale.profitUsd),
                    sale.paymentMethod,
                    sale.paymentReference,
                    sale.paymentStatus,
                    sale.notes
                )
                writer.write(line + "\n")
            }
            writer.flush()
            writer.close()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportProductsToCsv(context: Context, products: List<ProductEntity>): Uri? {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            exportDir.mkdirs()
            val fileName = "Inventario_VenVentas_${sdf.format(Date())}.csv"
            val file = File(exportDir, fileName)

            val writer = FileWriter(file)
            writer.write("\uFEFF")
            writer.write("ID,Codigo,Producto,Categoria,Costo USD,Precio Venta USD,Margen Ganancia %,Stock,Alerta Minima,Estado\n")

            for (p in products) {
                val line = buildCsvRow(
                    p.id.toString(),
                    p.code,
                    p.name,
                    p.category,
                    String.format(Locale.US, "%.2f", p.costPriceUsd),
                    String.format(Locale.US, "%.2f", p.salePriceUsd),
                    String.format(Locale.US, "%.1f%%", p.profitMarginPercent),
                    p.stockQuantity.toString(),
                    p.minStockAlert.toString(),
                    if (p.isActive) "Activo" else "Inactivo"
                )
                writer.write(line + "\n")
            }
            writer.flush()
            writer.close()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportCreditsToCsv(context: Context, accounts: List<CreditAccountEntity>): Uri? {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            exportDir.mkdirs()
            val fileName = "Creditos_Fiados_${sdf.format(Date())}.csv"
            val file = File(exportDir, fileName)

            val writer = FileWriter(file)
            writer.write("\uFEFF")
            writer.write("Cliente,Cedula,Telefono,Total Deuda USD,Total Pagado USD,Saldo Pendiente USD,Ultima Compra,Fecha Quincena,Notas\n")

            for (a in accounts) {
                val line = buildCsvRow(
                    a.customerName,
                    a.customerCedula,
                    a.customerPhone,
                    String.format(Locale.US, "%.2f", a.totalDebtUsd),
                    String.format(Locale.US, "%.2f", a.totalPaidUsd),
                    String.format(Locale.US, "%.2f", a.balancePendingUsd),
                    dateDisplay.format(Date(a.lastPurchaseTimestamp)),
                    a.nextQuincenaDueDate,
                    a.notes
                )
                writer.write(line + "\n")
            }
            writer.flush()
            writer.close()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareCsvFile(context: Context, uri: Uri, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Adjunto reporte exportado en formato CSV/Excel desde VenVentas POS.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Exportar y Compartir Reporte CSV / Excel")
        context.startActivity(chooser)
    }

    private fun buildCsvRow(vararg fields: String): String {
        return fields.joinToString(",") { field ->
            val escaped = field.replace("\"", "\"\"")
            if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
                "\"$escaped\""
            } else {
                escaped
            }
        }
    }
}
