package com.example.data

import android.content.Context
import android.content.SharedPreferences

data class MerchantProfile(
    val businessName: String,
    val rif: String,
    val phonePagoMovil: String,
    val bankName: String,
    val cedulaOwner: String,
    val thankYouMessage: String,
    val webhookApiUrl: String
)

class MerchantPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("merchant_settings", Context.MODE_PRIVATE)

    fun getProfile(): MerchantProfile {
        return MerchantProfile(
            businessName = prefs.getString("business_name", "Mi Negocio Vnzla") ?: "Mi Negocio Vnzla",
            rif = prefs.getString("rif", "J-50123456-7") ?: "J-50123456-7",
            phonePagoMovil = prefs.getString("phone_pago_movil", "04121234567") ?: "04121234567",
            bankName = prefs.getString("bank_name", "Banco de Venezuela (0102)") ?: "Banco de Venezuela (0102)",
            cedulaOwner = prefs.getString("cedula_owner", "V-20123456") ?: "V-20123456",
            thankYouMessage = prefs.getString("thank_you_msg", "¡Gracias por su compra! Dios bendiga su hogar.") ?: "¡Gracias por su compra! Dios bendiga su hogar.",
            webhookApiUrl = prefs.getString("webhook_api_url", "https://api.callmebot.com/whatsapp.php") ?: "https://api.callmebot.com/whatsapp.php"
        )
    }

    fun saveProfile(profile: MerchantProfile) {
        prefs.edit()
            .putString("business_name", profile.businessName)
            .putString("rif", profile.rif)
            .putString("phone_pago_movil", profile.phonePagoMovil)
            .putString("bank_name", profile.bankName)
            .putString("cedula_owner", profile.cedulaOwner)
            .putString("thank_you_msg", profile.thankYouMessage)
            .putString("webhook_api_url", profile.webhookApiUrl)
            .apply()
    }
}
