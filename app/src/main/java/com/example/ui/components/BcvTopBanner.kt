package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BcvRate
import com.example.ui.theme.BentoBlueContainer
import com.example.ui.theme.BentoBlueDark
import com.example.ui.theme.BentoBluePrimary
import com.example.ui.theme.BentoCardBg
import com.example.ui.theme.BentoDarkText
import com.example.ui.theme.BentoMintContainer
import com.example.ui.theme.BentoMintText
import com.example.ui.theme.BentoSlateLabel
import com.example.ui.theme.BentoSlateLight
import com.example.ui.theme.EmeraldSuccess
import com.example.util.TicketGenerator

@Composable
fun BcvTopBanner(
    bcvRate: BcvRate,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onSetCustomRate: (Double) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var inputRateText by remember { mutableStateOf(bcvRate.usdRate.toString()) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)),
        color = BentoCardBg,
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Bento Header Rate
                Column(
                    modifier = Modifier.clickable { showEditDialog = true }
                ) {
                    Text(
                        text = "TASA OFICIAL BCV",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = BentoSlateLabel
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = TicketGenerator.formatBs(bcvRate.usdRate).replace("Bs. ", "").trim(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoDarkText
                        )

                        Text(
                            text = "Bs/$",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoSlateLight
                        )

                        // Live pulse dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .alpha(pulseAlpha)
                                .clip(CircleShape)
                                .background(if (bcvRate.isWeekend) Color(0xFFF59E0B) else EmeraldSuccess)
                        )
                    }

                    Text(
                        text = if (bcvRate.isWeekend) "Fin de semana (Último cierre)" else bcvRate.lastUpdated,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = BentoSlateLight
                    )
                }

                // Bento Action Pill Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Edit Rate Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                inputRateText = bcvRate.usdRate.toString()
                                showEditDialog = true
                            }
                            .testTag("edit_bcv_rate_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modificar Tasa",
                                tint = BentoDarkText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Refresh Rate Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BentoBlueContainer,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = !isLoading) { onRefresh() }
                            .testTag("refresh_bcv_rate_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = BentoBlueDark,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Actualizar Tasa BCV",
                                    tint = BentoBlueDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (bcvRate.isWeekend) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF92400E),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BCV no cotiza fines de semana. Tasa de cierre activa.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = BentoCardBg,
            title = {
                Text(
                    text = "Ajustar Tasa Oficial BCV",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = BentoDarkText
                )
            },
            text = {
                Column {
                    Text(
                        text = "Fijar tasa oficial personalizada para el cálculo en Bolívares:",
                        fontSize = 13.sp,
                        color = BentoSlateLabel
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputRateText,
                        onValueChange = { inputRateText = it },
                        label = { Text("Tasa Bs. por 1 USD") },
                        prefix = { Text("Bs. ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().testTag("custom_bcv_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = inputRateText.replace(",", ".").toDoubleOrNull()
                        if (parsed != null && parsed > 0.0) {
                            onSetCustomRate(parsed)
                            showEditDialog = false
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
                    modifier = Modifier.testTag("save_custom_rate_button")
                ) {
                    Text("Guardar Tasa", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showEditDialog = false },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Cancelar", color = BentoSlateLabel)
                }
            }
        )
    }
}
