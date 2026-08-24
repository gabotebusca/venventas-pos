package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.analytics.AnalyticsScreen
import com.example.ui.components.BcvTopBanner
import com.example.ui.converter.CurrencyConverterScreen
import com.example.ui.credits.CreditsScreen
import com.example.ui.history.SalesHistoryScreen
import com.example.ui.inventory.InventoryScreen
import com.example.ui.pos.PosScreen
import com.example.ui.pos.TicketPreviewDialog
import com.example.ui.quincena.QuincenaScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.splash.BiblicalSplashScreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val isSplashFinished by viewModel.isSplashFinished.collectAsStateWithLifecycle()
    val splashCountdown by viewModel.splashCountdown.collectAsStateWithLifecycle()
    val bcvRate by viewModel.bcvRate.collectAsStateWithLifecycle()
    val isRateLoading by viewModel.isRateLoading.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val searchQuery by viewModel.productSearchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val sales by viewModel.allSales.collectAsStateWithLifecycle()
    val creditAccounts by viewModel.allCreditAccounts.collectAsStateWithLifecycle()
    val merchantProfile by viewModel.merchantProfile.collectAsStateWithLifecycle()
    val ticketPreview by viewModel.ticketPreview.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Request Notification permission if Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // 5-Second Biblical Splash Screen on startup
    if (!isSplashFinished) {
        BiblicalSplashScreen(
            verse = viewModel.biblicalVerse,
            bcvRate = bcvRate,
            countdownSeconds = splashCountdown,
            onSkip = { viewModel.skipSplash() }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                BcvTopBanner(
                    bcvRate = bcvRate,
                    isLoading = isRateLoading,
                    onRefresh = { viewModel.loadBcvRate() },
                    onSetCustomRate = { viewModel.setCustomBcvRate(it) }
                )
            },
            bottomBar = {
                AppBottomNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.setTab(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    AppTab.POS -> {
                        PosScreen(
                            viewModel = viewModel,
                            products = products,
                            cartItems = cartItems,
                            bcvRate = bcvRate.usdRate,
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory
                        )
                    }
                    AppTab.INVENTORY -> {
                        InventoryScreen(
                            products = allProducts,
                            bcvRate = bcvRate.usdRate,
                            onSaveProduct = { viewModel.saveProduct(it) },
                            onDeleteProduct = { viewModel.deleteProduct(it) },
                            onExportCsv = { viewModel.exportProductsCsv() }
                        )
                    }
                    AppTab.HISTORY -> {
                        SalesHistoryScreen(
                            sales = sales,
                            merchantProfile = merchantProfile,
                            onExportCsv = { viewModel.exportSalesCsv() },
                            onViewTicket = { sale ->
                                // Reconstruct ticket for review
                                val items = viewModel.repository.parseSaleItems(sale.itemsJson)
                                val bitmap = com.example.util.TicketGenerator.createTicketBitmap(
                                    viewModel.getApplication(),
                                    sale,
                                    items,
                                    merchantProfile
                                )
                                val uri = com.example.util.TicketGenerator.saveTicketImage(
                                    viewModel.getApplication(),
                                    bitmap,
                                    "Ticket_${sale.invoiceNumber}"
                                )
                                val waText = com.example.util.TicketGenerator.buildWhatsAppTextMessage(
                                    sale,
                                    items,
                                    merchantProfile
                                )
                                // We can trigger WhatsApp directly or share
                                if (uri != null) {
                                    com.example.util.TicketGenerator.shareTicket(
                                        viewModel.getApplication(),
                                        uri,
                                        waText,
                                        sale.customerPhone
                                    )
                                }
                            }
                        )
                    }
                    AppTab.CREDITS -> {
                        CreditsScreen(
                            creditAccounts = creditAccounts,
                            bcvRate = bcvRate.usdRate,
                            merchantProfile = merchantProfile,
                            onRecordPayment = { account, amountUsd, method, ref, notes ->
                                viewModel.recordCreditPayment(account, amountUsd, method, ref, notes)
                            },
                            onExportCsv = { viewModel.exportCreditsCsv() }
                        )
                    }
                    AppTab.CONVERTER -> {
                        CurrencyConverterScreen(bcvRate = bcvRate)
                    }
                    AppTab.QUINCENA -> {
                        QuincenaScreen(
                            debtors = creditAccounts,
                            bcvRate = bcvRate.usdRate,
                            merchantProfile = merchantProfile,
                            onTriggerNotification = { viewModel.sendQuincenaNotificationNow() }
                        )
                    }
                    AppTab.ANALYTICS -> {
                        AnalyticsScreen(
                            sales = sales,
                            bcvRate = bcvRate.usdRate
                        )
                    }
                    AppTab.SETTINGS -> {
                        SettingsScreen(
                            profile = merchantProfile,
                            bcvRate = bcvRate,
                            onSaveProfile = { viewModel.saveMerchantProfile(it) },
                            onExportSalesCsv = { viewModel.exportSalesCsv() },
                            onExportProductsCsv = { viewModel.exportProductsCsv() },
                            onExportCreditsCsv = { viewModel.exportCreditsCsv() }
                        )
                    }
                }
            }
        }

        // Ticket preview dialog when sale is completed
        ticketPreview?.let { state ->
            TicketPreviewDialog(
                ticketState = state,
                onDismiss = { viewModel.closeTicketPreview() }
            )
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = com.example.ui.theme.BentoCardBg,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
    ) {
        val tabs = listOf(
            AppTab.POS to Icons.Default.ShoppingCart,
            AppTab.INVENTORY to Icons.Default.Inventory2,
            AppTab.HISTORY to Icons.Default.ReceiptLong,
            AppTab.CREDITS to Icons.Default.AccountBalanceWallet,
            AppTab.CONVERTER to Icons.Default.CurrencyExchange,
            AppTab.QUINCENA to Icons.Default.NotificationsActive,
            AppTab.ANALYTICS to Icons.Default.Insights,
            AppTab.SETTINGS to Icons.Default.Settings
        )

        tabs.forEach { (tab, icon) ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = com.example.ui.theme.BentoBlueDark,
                    selectedTextColor = com.example.ui.theme.BentoBlueDark,
                    indicatorColor = com.example.ui.theme.BentoBlueContainer,
                    unselectedIconColor = com.example.ui.theme.BentoSlateLabel,
                    unselectedTextColor = com.example.ui.theme.BentoSlateLabel
                ),
                modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
            )
        }
    }
}
