package com.example.ui.credits

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MerchantProfile
import com.example.data.local.entities.CreditAccountEntity
import com.example.model.PaymentMethod
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

@Composable
fun CreditsScreen(
    creditAccounts: List<CreditAccountEntity>,
    bcvRate: Double,
    merchantProfile: MerchantProfile,
    onRecordPayment: (account: CreditAccountEntity, amountUsd: Double, method: PaymentMethod, ref: String, notes: String) -> Unit,
    onExportCsv: () -> Unit
) {
    val context = LocalContext.current
    var selectedAccountForPayment by remember { mutableStateOf<CreditAccountEntity?>(null) }

    val totalPendingUsd = creditAccounts.sumOf { it.balancePendingUsd }
    val totalPendingBs = totalPendingUsd * bcvRate
    val activeDebtorsCount = creditAccounts.count { it.balancePendingUsd > 0.01 }

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
                            text = "SISTEMA DE CRÉDITO Y FIADOS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = BentoSlateLabel
                        )
                        Text(
                            text = "Créditos por Cobrar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoDarkText
                        )
                    }

                    Button(
                        onClick = onExportCsv,
                        colors = ButtonDefaults.buttonColors(containerColor = BentoBlueDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("export_credits_csv_button")
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

                // Bento 2-Column Summary in Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = BentoDarkCard,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("POR COBRAR ($)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                            Text(
                                text = TicketGenerator.formatUsd(totalPendingUsd),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "${TicketGenerator.formatBs(totalPendingBs)} BCV",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoMintContainer
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = BentoMintContainer,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("DEUDORES ACTIVOS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BentoMintText.copy(alpha = 0.8f))
                            Text(
                                text = "$activeDebtorsCount Clientes",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = BentoMintText
                            )
                            Text(
                                text = "Cobro quincenal 15 / 30",
                                fontSize = 11.sp,
                                color = BentoMintText.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Debtors List
        if (creditAccounts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = BentoSlateLight,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No tienes cuentas de fiados activas",
                        color = BentoDarkText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Las ventas a Crédito se listarán aquí automáticamente.",
                        color = BentoSlateLabel,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(creditAccounts, key = { it.id }) { account ->
                    CreditAccountCard(
                        account = account,
                        bcvRate = bcvRate,
                        onCobrarWhatsApp = {
                            QuincenaManager.sendWhatsAppReminder(context, account, bcvRate, merchantProfile)
                        },
                        onRecordPayment = { selectedAccountForPayment = account }
                    )
                }
            }
        }
    }

    // Record Payment / Abono Dialog
    selectedAccountForPayment?.let { account ->
        RecordPaymentDialog(
            account = account,
            bcvRate = bcvRate,
            onDismiss = { selectedAccountForPayment = null },
            onConfirm = { amountUsd, method, ref, notes ->
                onRecordPayment(account, amountUsd, method, ref, notes)
                selectedAccountForPayment = null
            }
        )
    }
}

@Composable
fun CreditAccountCard(
    account: CreditAccountEntity,
    bcvRate: Double,
    onCobrarWhatsApp: () -> Unit,
    onRecordPayment: () -> Unit
) {
    val balanceBs = account.balancePendingUsd * bcvRate
    val hasPending = account.balancePendingUsd > 0.01

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BentoCardBg),
        border = BorderStroke(1.dp, BentoBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = account.customerName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoDarkText
                    )
                    if (account.customerPhone.isNotEmpty() || account.customerCedula.isNotEmpty()) {
                        Text(
                            text = listOfNotNull(
                                if (account.customerPhone.isNotEmpty()) "Tlf: ${account.customerPhone}" else null,
                                if (account.customerCedula.isNotEmpty()) "CI: ${account.customerCedula}" else null
                            ).joinToString(" • "),
                            fontSize = 11.sp,
                            color = BentoSlateLabel
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (hasPending) Color(0xFFFFEBEE) else BentoMintContainer
                ) {
                    Text(
                        text = if (hasPending) "Pendiente" else "Solvente",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasPending) Color(0xFFC62828) else BentoMintText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Saldo Deudor:", fontSize = 11.sp, color = BentoSlateLabel)
                    Text(
                        text = TicketGenerator.formatUsd(account.balancePendingUsd),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (hasPending) Color(0xFFC62828) else BentoMintText
                    )
                    Text(
                        text = TicketGenerator.formatBs(balanceBs),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoBlueDark
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Pagado: ${TicketGenerator.formatUsd(account.totalPaidUsd)}",
                        fontSize = 11.sp,
                        color = BentoMintText,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Fecha de cobro: ${account.nextQuincenaDueDate}",
                        fontSize = 11.sp,
                        color = BentoSlateLabel
                    )
                }
            }

            HorizontalDivider(
                color = BentoBorderSubtle,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cobrar WhatsApp Button
                Button(
                    onClick = onCobrarWhatsApp,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Cobrar",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WhatsApp",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Registrar Abono Button
                Button(
                    onClick = onRecordPayment,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoBlueDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = "Abonar",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Abonar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RecordPaymentDialog(
    account: CreditAccountEntity,
    bcvRate: Double,
    onDismiss: () -> Unit,
    onConfirm: (amountUsd: Double, method: PaymentMethod, ref: String, notes: String) -> Unit
) {
    var amountUsdText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.PAGO_MOVIL) }
    var referenceText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var isFullPayment by remember { mutableStateOf(false) }

    val parsedUsd = amountUsdText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val equivalentBs = parsedUsd * bcvRate

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BentoCardBg,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Registrar Abono - ${account.customerName}",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = BentoDarkText
            )
        },
        text = {
            Column {
                Text(
                    text = "Saldo Pendiente: ${TicketGenerator.formatUsd(account.balancePendingUsd)} (${TicketGenerator.formatBs(account.balancePendingUsd * bcvRate)})",
                    fontSize = 13.sp,
                    color = Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountUsdText,
                    onValueChange = {
                        amountUsdText = it
                        isFullPayment = false
                    },
                    label = { Text("Monto del Abono ($)") },
                    prefix = { Text("$ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (parsedUsd > 0.0) {
                    Text(
                        text = "Equivalente en Bs: ${TicketGenerator.formatBs(equivalentBs)}",
                        fontSize = 12.sp,
                        color = BentoMintText,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Full Payment Button
                OutlinedButton(
                    onClick = {
                        amountUsdText = String.format(java.util.Locale.US, "%.2f", account.balancePendingUsd)
                        isFullPayment = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Saldar Total (${TicketGenerator.formatUsd(account.balancePendingUsd)})", fontSize = 12.sp, color = BentoBlueDark)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = referenceText,
                    onValueChange = { referenceText = it },
                    label = { Text("N° de Referencia / Banco") },
                    placeholder = { Text("Ej: Pago Móvil #9382") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notas (Opcional)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (parsedUsd > 0.0) {
                        onConfirm(parsedUsd, selectedMethod, referenceText.trim(), notesText.trim())
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary)
            ) {
                Text("Guardar Abono", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(16.dp)) {
                Text("Cancelar", color = BentoSlateLabel)
            }
        }
    )
}
