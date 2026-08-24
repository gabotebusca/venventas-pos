package com.example.ui.quincena

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.local.entities.CreditAccountEntity
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
import com.example.util.QuincenaManager
import com.example.util.TicketGenerator
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun QuincenaScreen(
    debtors: List<CreditAccountEntity>,
    bcvRate: Double,
    merchantProfile: MerchantProfile,
    onTriggerNotification: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pendingDebtors = debtors.filter { it.balancePendingUsd > 0.01 }
    val totalPendingUsd = pendingDebtors.sumOf { it.balancePendingUsd }
    val totalPendingBs = totalPendingUsd * bcvRate

    var apiLoadingAccountId by remember { mutableStateOf<Long?>(null) }
    var apiStatusMessage by remember { mutableStateOf<String?>(null) }

    val cal = Calendar.getInstance()
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val isQuincenaDay = (day in 14..16) || (day in 28..31)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBackground)
    ) {
        // Bento Quincena Header
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
                            text = "RECORDATORIOS DE PAGO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = BentoSlateLabel
                        )
                        Text(
                            text = "Cobros Quincena (15 y 30)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoDarkText
                        )
                    }

                    Button(
                        onClick = onTriggerNotification,
                        colors = ButtonDefaults.buttonColors(containerColor = BentoBlueDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("trigger_notification_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Notificar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bento Grid Top Metrics (Bento Style: 2 Column Layout)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Bento Mint Box for Quincena status (as in HTML Bento mock)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoMintContainer),
                        border = BorderStroke(1.dp, Color(0xFFBBE5D8))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = BentoMintText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = if (isQuincenaDay) "COBRO HOY" else "PRÓX. QUINCENA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = BentoMintText.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = if (isQuincenaDay) "Día de Cobro" else "Día 15 / 30",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BentoMintText
                                )
                                Text(
                                    text = "Recordatorio WhatsApp",
                                    fontSize = 9.sp,
                                    color = BentoMintText.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Bento Card for Pending Debtors Total
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoCardBg),
                        border = BorderStroke(1.dp, BentoBorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "POR COBRAR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = BentoSlateLabel
                            )
                            Column {
                                Text(
                                    text = TicketGenerator.formatUsd(totalPendingUsd),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BentoBlueDark
                                )
                                Text(
                                    text = TicketGenerator.formatBs(totalPendingBs),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlateLabel
                                )
                            }
                            Text(
                                text = "${pendingDebtors.size} clientes fiados",
                                fontSize = 10.sp,
                                color = BentoSlateLight
                            )
                        }
                    }
                }
            }

            // Webhook / API status banner
            apiStatusMessage?.let { status ->
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BentoBlueContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BentoBlueDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = status, fontSize = 12.sp, color = BentoBlueDark, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Debtor List Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CLIENTES CON CUENTAS ACTIVAS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = BentoSlateLabel
                    )
                    Text(
                        text = "${pendingDebtors.size} Registros",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoBluePrimary
                    )
                }
            }

            if (pendingDebtors.isEmpty()) {
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
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎉", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No tienes deudas pendientes",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoDarkText
                                )
                                Text(
                                    text = "Todas las cuentas de quincena están al día.",
                                    fontSize = 12.sp,
                                    color = BentoSlateLabel
                                )
                            }
                        }
                    }
                }
            } else {
                items(pendingDebtors, key = { it.id }) { debtor ->
                    DebtorBentoCard(
                        debtor = debtor,
                        bcvRate = bcvRate,
                        merchantProfile = merchantProfile,
                        isApiLoading = apiLoadingAccountId == debtor.id,
                        onSendWhatsApp = {
                            QuincenaManager.sendWhatsAppReminder(
                                context = context,
                                debtor = debtor,
                                bcvRate = bcvRate,
                                merchant = merchantProfile
                            )
                        },
                        onTriggerWebhook = {
                            scope.launch {
                                apiLoadingAccountId = debtor.id
                                val result = QuincenaManager.sendAutomatedExternalApiReminder(
                                    debtor = debtor,
                                    bcvRate = bcvRate,
                                    merchant = merchantProfile
                                )
                                apiLoadingAccountId = null
                                apiStatusMessage = if (result.isSuccess) {
                                    "✅ Webhook enviado con éxito para ${debtor.customerName}"
                                } else {
                                    "⚠️ ${result.exceptionOrNull()?.message ?: "Revisa la URL de webhook en Configuración"}"
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DebtorBentoCard(
    debtor: CreditAccountEntity,
    bcvRate: Double,
    merchantProfile: MerchantProfile,
    isApiLoading: Boolean,
    onSendWhatsApp: () -> Unit,
    onTriggerWebhook: () -> Unit
) {
    val pendingBs = debtor.balancePendingUsd * bcvRate

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Client Avatar & Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BentoBackground,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = debtor.customerName.take(2).uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = BentoDarkText
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = debtor.customerName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoDarkText
                        )
                        Text(
                            text = if (debtor.customerPhone.isNotBlank()) debtor.customerPhone else "Sin teléfono",
                            fontSize = 11.sp,
                            color = BentoSlateLabel
                        )
                    }
                }

                // Balance
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = TicketGenerator.formatUsd(debtor.balancePendingUsd),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = BentoBlueDark
                    )
                    Text(
                        text = TicketGenerator.formatBs(pendingBs),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoSlateLabel
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSendWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recordar por WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onTriggerWebhook,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(44.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    if (isApiLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = BentoBluePrimary)
                    } else {
                        Icon(Icons.Default.Api, contentDescription = "Webhook API", tint = BentoBlueDark, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
