package com.example.ui.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MerchantProfile
import com.example.data.local.entities.SaleEntity
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoBlueContainer
import com.example.ui.theme.BentoBlueDark
import com.example.ui.theme.BentoBluePrimary
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoBorderSubtle
import com.example.ui.theme.BentoCardBg
import com.example.ui.theme.BentoDarkCard
import com.example.ui.theme.BentoDarkText
import com.example.ui.theme.BentoMintContainer
import com.example.ui.theme.BentoMintText
import com.example.ui.theme.BentoSlateLabel
import com.example.ui.theme.BentoSlateLight
import com.example.util.TicketGenerator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SalesHistoryScreen(
    sales: List<SaleEntity>,
    merchantProfile: MerchantProfile,
    onExportCsv: () -> Unit,
    onViewTicket: (SaleEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("Todos") } // "Hoy", "Esta Semana", "Este Mes", "Todos"

    val now = Calendar.getInstance()
    val filteredSales = sales.filter { sale ->
        val saleCal = Calendar.getInstance().apply { timeInMillis = sale.timestamp }
        when (selectedFilter) {
            "Hoy" -> saleCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    saleCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
            "Esta Semana" -> saleCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    saleCal.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR)
            "Este Mes" -> saleCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    saleCal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
            else -> true
        }
    }

    val totalUsd = filteredSales.sumOf { it.totalUsd }
    val totalBs = filteredSales.sumOf { it.totalBs }
    val totalProfit = filteredSales.sumOf { it.profitUsd }

    val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBackground)
    ) {
        // Bento Summary Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BentoCardBg,
            shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
            border = BorderStroke(1.dp, BentoBorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "REGISTRO DE OPERACIONES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = BentoSlateLabel
                        )
                        Text(
                            text = "Historial de Ventas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoDarkText
                        )
                    }

                    Button(
                        onClick = onExportCsv,
                        colors = ButtonDefaults.buttonColors(containerColor = BentoBlueDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("export_sales_csv_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Exportar",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Exportar CSV", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bento Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("Todos", "Hoy", "Esta Semana", "Este Mes")) { filter ->
                        val isSelected = filter == selectedFilter
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) BentoBlueDark else BentoBackground,
                            border = if (isSelected) null else BorderStroke(1.dp, BentoBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedFilter = filter }
                        ) {
                            Text(
                                text = filter,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else BentoSlateLabel,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Bento 3-Metric Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoDarkCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "TOTAL USD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                            Text(text = TicketGenerator.formatUsd(totalUsd), fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Column {
                            Text(text = "TOTAL BS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                            Text(text = TicketGenerator.formatBs(totalBs), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoMintContainer)
                        }
                        Column {
                            Text(text = "GANANCIA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                            Text(text = "+${TicketGenerator.formatUsd(totalProfit)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = BentoMintContainer)
                        }
                    }
                }
            }

            if (filteredSales.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoCardBg),
                        border = BorderStroke(1.dp, BentoBorderSubtle)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🧾", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No hay ventas para este filtro",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoDarkText
                                )
                                Text(
                                    text = "Las ventas realizadas aparecerán aquí en orden cronológico",
                                    fontSize = 12.sp,
                                    color = BentoSlateLabel
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredSales, key = { it.id }) { sale ->
                    SaleBentoCard(
                        sale = sale,
                        formattedDate = sdf.format(Date(sale.timestamp)),
                        onViewTicket = { onViewTicket(sale) },
                        onShareWhatsApp = {
                            val msg = TicketGenerator.buildWhatsAppSaleText(sale, merchantProfile)
                            val url = TicketGenerator.getWhatsAppDirectUrl(sale.customerPhone, msg)
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, msg)
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Enviar Comprobante"))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SaleBentoCard(
    sale: SaleEntity,
    formattedDate: String,
    onViewTicket: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BentoCardBg),
        border = BorderStroke(1.dp, BentoBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BentoBackground,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = BentoBlueDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "#${sale.invoiceNumber} • ${sale.customerName}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoDarkText
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 11.sp,
                            color = BentoSlateLabel
                        )
                    }
                }

                // Payment Method Chip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BentoBlueContainer
                ) {
                    Text(
                        text = sale.paymentMethod,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoBlueDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = BentoBorderSubtle, modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = TicketGenerator.formatUsd(sale.totalUsd),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoMintText
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = TicketGenerator.formatBs(sale.totalBs),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoSlateLabel
                        )
                    }
                    Text(
                        text = "Ganancia: +${TicketGenerator.formatUsd(sale.profitUsd)}",
                        fontSize = 10.sp,
                        color = BentoMintText,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onShareWhatsApp,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "WhatsApp", tint = Color(0xFF25D366), modifier = Modifier.size(18.dp))
                    }

                    Button(
                        onClick = onViewTicket,
                        colors = ButtonDefaults.buttonColors(containerColor = BentoBackground),
                        border = BorderStroke(1.dp, BentoBorder),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Ver Ticket", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoDarkText)
                    }
                }
            }
        }
    }
}
