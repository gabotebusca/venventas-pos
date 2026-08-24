package com.example.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CartItem
import com.example.model.PaymentMethod
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SlateBorder
import com.example.util.TicketGenerator

@Composable
fun CheckoutDialog(
    cartItems: List<CartItem>,
    bcvRate: Double,
    onDismiss: () -> Unit,
    onConfirmSale: (
        customerName: String,
        customerPhone: String,
        customerCedula: String,
        paymentMethod: PaymentMethod,
        paymentReference: String,
        notes: String
    ) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerCedula by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.PAGO_MOVIL) }
    var paymentReference by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    val totalUsd = cartItems.sumOf { it.totalUsd }
    val totalBs = totalUsd * bcvRate

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        title = {
            Column {
                Text(
                    text = "Procesar Venta & Ticket",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Text(
                    text = "Total a Cobrar: ${TicketGenerator.formatUsd(totalUsd)} • ${TicketGenerator.formatBs(totalBs)}",
                    fontSize = 13.sp,
                    color = ForestGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Customer Name (MANDATORY for ticket generation)
                Text(
                    text = "1. Datos del Cliente (Para el Ticket):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = {
                        customerName = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("Nombre del Cliente *") },
                    placeholder = { Text("Ej: Carlos Rodríguez") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary)
                    },
                    isError = nameError,
                    supportingText = {
                        if (nameError) Text("El nombre es requerido para emitir el ticket", color = Color.Red)
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_customer_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("WhatsApp / Tlf") },
                        placeholder = { Text("04121234567") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = ForestGreen)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("checkout_customer_phone_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = customerCedula,
                        onValueChange = { customerCedula = it },
                        label = { Text("C.I. / RIF") },
                        placeholder = { Text("V-12345678") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("checkout_customer_cedula_input")
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = SlateBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // 2. Payment Method selection
                Text(
                    text = "2. Elije tu Método de Pago:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Payment Methods List
                PaymentMethodOption(
                    method = PaymentMethod.PAGO_MOVIL,
                    icon = Icons.Default.PhoneAndroid,
                    selected = selectedPaymentMethod == PaymentMethod.PAGO_MOVIL,
                    onClick = { selectedPaymentMethod = PaymentMethod.PAGO_MOVIL }
                )
                PaymentMethodOption(
                    method = PaymentMethod.TRANSFERENCIA,
                    icon = Icons.Default.AccountBalance,
                    selected = selectedPaymentMethod == PaymentMethod.TRANSFERENCIA,
                    onClick = { selectedPaymentMethod = PaymentMethod.TRANSFERENCIA }
                )
                PaymentMethodOption(
                    method = PaymentMethod.CREDITO,
                    icon = Icons.Default.ReceiptLong,
                    selected = selectedPaymentMethod == PaymentMethod.CREDITO,
                    onClick = { selectedPaymentMethod = PaymentMethod.CREDITO },
                    isCreditBadge = true
                )
                PaymentMethodOption(
                    method = PaymentMethod.BIOPAGO,
                    icon = Icons.Default.Fingerprint,
                    selected = selectedPaymentMethod == PaymentMethod.BIOPAGO,
                    onClick = { selectedPaymentMethod = PaymentMethod.BIOPAGO }
                )
                PaymentMethodOption(
                    method = PaymentMethod.PUNTO_VENTA,
                    icon = Icons.Default.CreditCard,
                    selected = selectedPaymentMethod == PaymentMethod.PUNTO_VENTA,
                    onClick = { selectedPaymentMethod = PaymentMethod.PUNTO_VENTA }
                )
                PaymentMethodOption(
                    method = PaymentMethod.EFECTIVO_USD,
                    icon = Icons.Default.AttachMoney,
                    selected = selectedPaymentMethod == PaymentMethod.EFECTIVO_USD,
                    onClick = { selectedPaymentMethod = PaymentMethod.EFECTIVO_USD }
                )
                PaymentMethodOption(
                    method = PaymentMethod.EFECTIVO_BS,
                    icon = Icons.Default.Payments,
                    selected = selectedPaymentMethod == PaymentMethod.EFECTIVO_BS,
                    onClick = { selectedPaymentMethod = PaymentMethod.EFECTIVO_BS }
                )
                PaymentMethodOption(
                    method = PaymentMethod.MIXTO,
                    icon = Icons.Default.SwapHoriz,
                    selected = selectedPaymentMethod == PaymentMethod.MIXTO,
                    onClick = { selectedPaymentMethod = PaymentMethod.MIXTO }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Reference / Bank Field if needed
                if (selectedPaymentMethod.requiresReference) {
                    OutlinedTextField(
                        value = paymentReference,
                        onValueChange = { paymentReference = it },
                        label = { Text("N° de Referencia / Banco Emisor") },
                        placeholder = { Text("Ej: Ref #4829 - Banesco") },
                        leadingIcon = {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = NavyPrimary)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_reference_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (selectedPaymentMethod == PaymentMethod.CREDITO) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ℹ️ Esta venta se registrará en la cuenta de créditos/fiados de ${customerName.ifBlank { "este cliente" }} para cobro el 15 o 30 de mes.",
                            fontSize = 12.sp,
                            color = Color(0xFF856404),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas adicionales (Opcional)") },
                    placeholder = { Text("Detalles del despacho o entrega...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customerName.isBlank()) {
                        nameError = true
                    } else {
                        onConfirmSale(
                            customerName.trim(),
                            customerPhone.trim(),
                            customerCedula.trim(),
                            selectedPaymentMethod,
                            paymentReference.trim(),
                            notes.trim()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_sale_button")
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generar Ticket y Cobrar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun PaymentMethodOption(
    method: PaymentMethod,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    isCreditBadge: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) NavyPrimary.copy(alpha = 0.08f) else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) NavyPrimary else SlateBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = if (selected) NavyPrimary else Color(0xFFF1F5F9),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = method.displayName,
                            tint = if (selected) Color.White else NavyPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = method.displayName,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) NavyPrimary else Color(0xFF334155)
                )
            }

            if (isCreditBadge) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFE082)
                ) {
                    Text(
                        text = "Quincena",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
