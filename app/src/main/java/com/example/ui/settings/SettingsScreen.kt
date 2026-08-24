package com.example.ui.settings

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MerchantProfile
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

@Composable
fun SettingsScreen(
    profile: MerchantProfile,
    bcvRate: BcvRate,
    onSaveProfile: (MerchantProfile) -> Unit,
    onExportSalesCsv: () -> Unit,
    onExportProductsCsv: () -> Unit,
    onExportCreditsCsv: () -> Unit
) {
    var businessName by remember { mutableStateOf(profile.businessName) }
    var rif by remember { mutableStateOf(profile.rif) }
    var phonePagoMovil by remember { mutableStateOf(profile.phonePagoMovil) }
    var bankName by remember { mutableStateOf(profile.bankName) }
    var cedulaOwner by remember { mutableStateOf(profile.cedulaOwner) }
    var thankYouMessage by remember { mutableStateOf(profile.thankYouMessage) }
    var webhookApiUrl by remember { mutableStateOf(profile.webhookApiUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Business Profile Bento Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            border = BorderStroke(1.dp, BentoBorderSubtle),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BentoBlueContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = BentoBlueDark, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Datos de tu Negocio en Venezuela",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoDarkText
                        )
                        Text(
                            text = "Aparecen en tus tickets de venta y mensajes de cobro",
                            fontSize = 11.sp,
                            color = BentoSlateLabel
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Nombre Comercial / Negocio") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = BentoBluePrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoBluePrimary,
                        unfocusedBorderColor = BentoBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("settings_business_name_input")
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = rif,
                        onValueChange = { rif = it },
                        label = { Text("RIF del Comercio") },
                        placeholder = { Text("J-12345678-9") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = cedulaOwner,
                        onValueChange = { cedulaOwner = it },
                        label = { Text("C.I. Titular Pago Móvil") },
                        placeholder = { Text("V-20123456") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = phonePagoMovil,
                    onValueChange = { phonePagoMovil = it },
                    label = { Text("Teléfono para Pago Móvil") },
                    leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = BentoMintText) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Banco Principal Pago Móvil") },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = BentoBluePrimary) },
                    placeholder = { Text("Banco de Venezuela (0102), Banesco (0134), etc.") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = thankYouMessage,
                    onValueChange = { thankYouMessage = it },
                    label = { Text("Mensaje de Bendición / Agradecimiento") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        onSaveProfile(
                            MerchantProfile(
                                businessName = businessName.trim(),
                                rif = rif.trim(),
                                phonePagoMovil = phonePagoMovil.trim(),
                                bankName = bankName.trim(),
                                cedulaOwner = cedulaOwner.trim(),
                                thankYouMessage = thankYouMessage.trim(),
                                webhookApiUrl = webhookApiUrl.trim()
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().testTag("save_settings_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Guardar Cambios del Negocio", fontWeight = FontWeight.Bold)
                }
            }
        }

        // External API / Webhook Automation Bento Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            border = BorderStroke(1.dp, BentoBorderSubtle),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BentoMintContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Api, contentDescription = null, tint = BentoMintText, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Integración API Externa (WhatsApp)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoDarkText
                        )
                        Text(
                            text = "Automatiza el envío de recordatorios en quincena",
                            fontSize = 11.sp,
                            color = BentoSlateLabel
                        )
                    }
                }

                Text(
                    text = "Puedes usar servicios como CallMeBot WhatsApp API, Twilio, Evolution API o tu propio Webhook Gateway:",
                    fontSize = 11.sp,
                    color = BentoSlateLight
                )

                OutlinedTextField(
                    value = webhookApiUrl,
                    onValueChange = { webhookApiUrl = it },
                    label = { Text("URL de Webhook / API WhatsApp") },
                    placeholder = { Text("https://api.callmebot.com/whatsapp.php") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Backup & CSV Export Center Bento Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = BentoCardBg),
            border = BorderStroke(1.dp, BentoBorderSubtle),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BentoBlueContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = BentoBlueDark, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Exportación de Datos (Excel / CSV)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoDarkText
                        )
                        Text(
                            text = "Tus datos se guardan en el teléfono de forma segura",
                            fontSize = 11.sp,
                            color = BentoSlateLabel
                        )
                    }
                }

                OutlinedButton(
                    onClick = onExportSalesCsv,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Exportar Historial de Ventas (.CSV)", color = BentoBlueDark, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onExportProductsCsv,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Exportar Catálogo de Productos (.CSV)", color = BentoBlueDark, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onExportCreditsCsv,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Exportar Cuentas por Cobrar / Fiados (.CSV)", color = BentoBlueDark, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
