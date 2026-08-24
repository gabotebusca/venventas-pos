package com.example.ui.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.EmeraldSuccess
import com.example.util.TicketGenerator
import java.util.Locale

@Composable
fun AnalyticsScreen(
    sales: List<SaleEntity>,
    bcvRate: Double
) {
    val totalRevenueUsd = sales.sumOf { it.totalUsd }
    val totalRevenueBs = totalRevenueUsd * bcvRate
    val totalCostUsd = sales.sumOf { it.totalCostUsd }
    val totalProfitUsd = sales.sumOf { it.profitUsd }
    val totalProfitBs = totalProfitUsd * bcvRate

    val profitMarginPercent = if (totalRevenueUsd > 0) (totalProfitUsd / totalRevenueUsd) * 100 else 0.0
    val averageTicketUsd = if (sales.isNotEmpty()) totalRevenueUsd / sales.size else 0.0

    // Group by Payment Method
    val salesByMethod = sales.groupBy { it.paymentMethod }
        .mapValues { entry -> entry.value.sumOf { it.totalUsd } }
        .toList()
        .sortedByDescending { it.second }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Main Bento Hero Profit Card (Dark Bento design)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = BentoDarkCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GANANCIA NETA TOTAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "+${TicketGenerator.formatUsd(totalProfitUsd)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "+${TicketGenerator.formatBs(totalProfitBs)} (Tasa BCV)",
                            fontSize = 13.sp,
                            color = BentoMintContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bento 2x2 Grid Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BentoStatCard(
                title = "Margen Utilidad",
                value = "+${String.format(Locale.US, "%.1f", profitMarginPercent)}%",
                subtitle = "Rentabilidad",
                icon = Icons.Default.Percent,
                color = BentoMintText,
                modifier = Modifier.weight(1f)
            )
            BentoStatCard(
                title = "Ventas Totales",
                value = TicketGenerator.formatUsd(totalRevenueUsd),
                subtitle = TicketGenerator.formatBs(totalRevenueBs),
                icon = Icons.Default.AttachMoney,
                color = BentoBlueDark,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BentoStatCard(
                title = "Ticket Promedio",
                value = TicketGenerator.formatUsd(averageTicketUsd),
                subtitle = "${sales.size} ventas hechas",
                icon = Icons.Default.ShoppingCart,
                color = BentoBluePrimary,
                modifier = Modifier.weight(1f)
            )
            BentoStatCard(
                title = "Costo Invertido",
                value = TicketGenerator.formatUsd(totalCostUsd),
                subtitle = "Costo de inventario",
                icon = Icons.Default.AccountBalanceWallet,
                color = BentoSlateLabel,
                modifier = Modifier.weight(1f)
            )
        }

        // Bento Distribution by Venezuelan Payment Methods
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            border = BorderStroke(1.dp, BentoBorderSubtle),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "CANALES DE PAGO EN VENEZUELA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = BentoSlateLabel
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (salesByMethod.isEmpty()) {
                    Text(
                        text = "Aún no se han registrado ventas",
                        fontSize = 13.sp,
                        color = BentoSlateLight
                    )
                } else {
                    salesByMethod.forEach { (method, amountUsd) ->
                        val percent = if (totalRevenueUsd > 0) (amountUsd / totalRevenueUsd).toFloat() else 0f
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = method,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BentoDarkText
                                )
                                Text(
                                    text = "${TicketGenerator.formatUsd(amountUsd)} (${String.format(Locale.US, "%.0f", percent * 100)}%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoDarkText
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { percent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = BentoBluePrimary,
                                trackColor = BentoBackground
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BentoStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BentoCardBg),
        border = BorderStroke(1.dp, BentoBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BentoBackground,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = BentoSlateLabel
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = BentoSlateLight,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
