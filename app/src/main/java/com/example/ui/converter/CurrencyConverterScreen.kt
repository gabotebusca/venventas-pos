package com.example.ui.converter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BcvRate
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CurrencyConverterScreen(
    bcvRate: BcvRate
) {
    var usdInput by remember { mutableStateOf("100") }
    var bsInput by remember { mutableStateOf("") }
    var lastEditedCurrency by remember { mutableStateOf("USD") } // "USD" or "BS"

    val rate = bcvRate.usdRate

    // Calculated amounts
    val parsedUsd = usdInput.replace(",", ".").toDoubleOrNull() ?: 0.0
    val calculatedBsFromUsd = parsedUsd * rate

    val parsedBs = bsInput.replace(",", ".").toDoubleOrNull() ?: 0.0
    val calculatedUsdFromBs = if (rate > 0) parsedBs / rate else 0.0

    // Vuelto / Cash Change Calculator states
    var billGivenUsd by remember { mutableStateOf("20") }
    var saleTotalUsd by remember { mutableStateOf("12.50") }

    val parsedBill = billGivenUsd.replace(",", ".").toDoubleOrNull() ?: 0.0
    val parsedSaleTotal = saleTotalUsd.replace(",", ".").toDoubleOrNull() ?: 0.0
    val changeUsd = (parsedBill - parsedSaleTotal).coerceAtLeast(0.0)
    val changeBs = changeUsd * rate

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Bento Convertidor Dual Card (as shown in Bento mock)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            border = BorderStroke(1.dp, BentoBorderSubtle),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "CONVERTIDOR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = BentoSlateLabel
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(java.util.Locale.US, "%.2f", if (lastEditedCurrency == "USD") parsedUsd else calculatedUsdFromBs),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoDarkText
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "$", fontSize = 13.sp, color = BentoSlateLight)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BentoBlueContainer,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            lastEditedCurrency = if (lastEditedCurrency == "USD") "BS" else "USD"
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "⇄", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BentoBlueDark)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "EQUIVALE A",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = BentoSlateLabel
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = TicketGenerator.formatBs(if (lastEditedCurrency == "USD") calculatedBsFromUsd else parsedBs).replace("Bs. ", ""),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoDarkText
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Bs", fontSize = 13.sp, color = BentoSlateLight)
                    }
                }
            }
        }

        // Main Bento Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            border = BorderStroke(1.dp, BentoBorderSubtle),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BentoBlueContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CurrencyExchange,
                                contentDescription = null,
                                tint = BentoBlueDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Calculadora Bidireccional $ ⇄ Bs",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoDarkText
                        )
                        Text(
                            text = "Tasa BCV: ${TicketGenerator.formatBs(rate)} por cada $1.00 USD",
                            fontSize = 11.sp,
                            color = BentoMintText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // USD Field
                OutlinedTextField(
                    value = if (lastEditedCurrency == "USD") usdInput else String.format(java.util.Locale.US, "%.2f", calculatedUsdFromBs),
                    onValueChange = {
                        usdInput = it
                        lastEditedCurrency = "USD"
                    },
                    label = { Text("Monto en Dólares ($ USD)") },
                    prefix = { Text("$ ", fontWeight = FontWeight.Bold, color = BentoMintText) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoBluePrimary,
                        unfocusedBorderColor = BentoBorder,
                        focusedContainerColor = BentoBackground,
                        unfocusedContainerColor = BentoBackground
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("converter_usd_input")
                )

                // Quick Chips (Bento Pills)
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("1", "5", "10", "20", "50", "100", "200").forEach { chipAmount ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = BentoBackground,
                            border = BorderStroke(1.dp, BentoBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    usdInput = chipAmount
                                    lastEditedCurrency = "USD"
                                }
                        ) {
                            Text(
                                text = "$$chipAmount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoDarkText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Swap indicator
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = BentoBlueContainer,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable {
                                lastEditedCurrency = if (lastEditedCurrency == "USD") "BS" else "USD"
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Intercambiar",
                                tint = BentoBlueDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // BS Field
                OutlinedTextField(
                    value = if (lastEditedCurrency == "BS") bsInput else String.format(java.util.Locale.US, "%.2f", calculatedBsFromUsd),
                    onValueChange = {
                        bsInput = it
                        lastEditedCurrency = "BS"
                    },
                    label = { Text("Monto en Bolívares (Bs. Digital)") },
                    prefix = { Text("Bs. ", fontWeight = FontWeight.Bold, color = BentoBluePrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoBluePrimary,
                        unfocusedBorderColor = BentoBorder,
                        focusedContainerColor = BentoBackground,
                        unfocusedContainerColor = BentoBackground
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("converter_bs_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Bento Result Summary
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BentoDarkCard,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (lastEditedCurrency == "USD") {
                                "$${if (usdInput.isBlank()) "0" else usdInput} USD equivalen a:"
                            } else {
                                "Bs. ${if (bsInput.isBlank()) "0" else bsInput} equivalen a:"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lastEditedCurrency == "USD") {
                                TicketGenerator.formatBs(calculatedBsFromUsd)
                            } else {
                                TicketGenerator.formatUsd(calculatedUsdFromBs)
                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoMintContainer
                        )
                    }
                }
            }
        }

        // Cash Change / Vuelto Calculator Bento Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            border = BorderStroke(1.dp, BentoBorderSubtle),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BentoMintContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = BentoMintText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Calculadora de Vuelto / Cambio",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoDarkText
                        )
                        Text(
                            text = "Calcula el vuelto en $ o en Bs. por Pago Móvil",
                            fontSize = 11.sp,
                            color = BentoSlateLabel
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = saleTotalUsd,
                        onValueChange = { saleTotalUsd = it },
                        label = { Text("Total Cuenta ($)") },
                        prefix = { Text("$ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = billGivenUsd,
                        onValueChange = { billGivenUsd = it },
                        label = { Text("Paga con ($)") },
                        prefix = { Text("$ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BentoMintContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "VUELTO A ENTREGAR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = BentoMintText.copy(alpha = 0.8f)
                            )
                            Text(
                                text = TicketGenerator.formatUsd(changeUsd),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = BentoMintText
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "En Bolívares (Pago Móvil):",
                                fontSize = 10.sp,
                                color = BentoMintText.copy(alpha = 0.8f)
                            )
                            Text(
                                text = TicketGenerator.formatBs(changeBs),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = BentoMintText
                            )
                        }
                    }
                }
            }
        }
    }
}
