package com.example.ui.pos

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.model.BiblicalVerse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ProductEntity
import com.example.model.CartItem
import com.example.model.PaymentMethod
import com.example.ui.MainViewModel
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PosScreen(
    viewModel: MainViewModel,
    products: List<ProductEntity>,
    cartItems: List<CartItem>,
    bcvRate: Double,
    searchQuery: String,
    selectedCategory: String
) {
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showCartSheet by remember { mutableStateOf(false) }
    val cartSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val categories = listOf("Todos", "Víveres", "Charcutería", "Bebidas", "Café y Bebidas", "Lácteos", "Limpieza", "Snacks")

    val totalCartUsd = cartItems.sumOf { it.totalUsd }
    val totalCartBs = totalCartUsd * bcvRate
    val totalCartItemsCount = cartItems.sumOf { it.quantity }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BentoBackground)
        ) {
            // Search Bar & Filter Bento Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BentoCardBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setProductSearch(it) },
                    placeholder = { Text("Buscar producto por nombre o código...", fontSize = 13.sp, color = BentoSlateLabel) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setProductSearch("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = BentoSlateLabel)
                            }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoBluePrimary,
                        unfocusedBorderColor = BentoBorder,
                        focusedContainerColor = BentoBackground,
                        unfocusedContainerColor = BentoBackground
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pos_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Categories Row (Pill chips)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) BentoBlueDark else BentoBackground,
                            border = if (isSelected) null else BorderStroke(1.dp, BentoBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.setSelectedCategory(category) }
                        ) {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else BentoSlateLabel,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Products Catalog
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No se encontraron productos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoSlateLabel
                        )
                        Text(
                            text = "Intenta otra búsqueda o agrega productos en Inventario",
                            fontSize = 13.sp,
                            color = BentoSlateLight
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = if (cartItems.isNotEmpty()) 120.dp else 24.dp)
                ) {
                    // Bento Daily Inspiration Card when at top
                    if (searchQuery.isEmpty() && selectedCategory == "Todos") {
                        item {
                            BentoDailyInspirationCard(verse = viewModel.biblicalVerse)
                        }
                    }

                    items(products, key = { it.id }) { product ->
                        val inCartQuantity = cartItems.find { it.productId == product.id }?.quantity ?: 0
                        ProductPosCard(
                            product = product,
                            bcvRate = bcvRate,
                            inCartQuantity = inCartQuantity,
                            onAddToCart = { viewModel.addToCart(product) },
                            onReduceQuantity = { viewModel.updateCartQuantity(product.id, inCartQuantity - 1) }
                        )
                    }
                }
            }
        }

        // Bottom Bento Floating Cart Summary Bar
        AnimatedVisibility(
            visible = cartItems.isNotEmpty(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                shape = RoundedCornerShape(28.dp),
                color = BentoDarkCard,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Cart Info & Click to open Sheet
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showCartSheet = true }
                            .padding(vertical = 4.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = BentoMintText) {
                                    Text(
                                        text = totalCartItemsCount.toString(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Carrito",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Total: ${TicketGenerator.formatUsd(totalCartUsd)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "equiv. ${TicketGenerator.formatBs(totalCartBs)}",
                                fontSize = 11.sp,
                                color = BentoMintContainer
                            )
                        }
                    }

                    // Process Sale Action Button
                    Button(
                        onClick = { showCheckoutDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("process_sale_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCartCheckout,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cobrar Venta",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet with Cart details
    if (showCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = cartSheetState,
            containerColor = BentoCardBg,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CARRITO DE COMPRA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = BentoSlateLabel
                        )
                        Text(
                            text = "$totalCartItemsCount productos seleccionados",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoDarkText
                        )
                    }

                    IconButton(
                        onClick = { viewModel.clearCart() },
                        modifier = Modifier.testTag("clear_cart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Vaciar",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cartItems, key = { it.productId }) { item ->
                        CartItemRow(
                            item = item,
                            bcvRate = bcvRate,
                            onIncrease = { viewModel.updateCartQuantity(item.productId, item.quantity + 1) },
                            onDecrease = { viewModel.updateCartQuantity(item.productId, item.quantity - 1) },
                            onRemove = { viewModel.removeFromCart(item.productId) }
                        )
                    }
                }

                HorizontalDivider(color = BentoBorder, modifier = Modifier.padding(vertical = 12.dp))

                // Totals in Bento Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BentoBackground,
                    border = BorderStroke(1.dp, BentoBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL A COBRAR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = BentoSlateLabel
                            )
                            Text(
                                text = TicketGenerator.formatUsd(totalCartUsd),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = BentoDarkText
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "TASA BCV: ${TicketGenerator.formatBs(bcvRate)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoMintText
                            )
                            Text(
                                text = TicketGenerator.formatBs(totalCartBs),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoBluePrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        showCartSheet = false
                        showCheckoutDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("checkout_from_cart_button")
                ) {
                    Text("PROCESAR COBRO", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Checkout Dialog (Venezuelan payment methods & required client name)
    if (showCheckoutDialog) {
        CheckoutDialog(
            cartItems = cartItems,
            totalUsd = totalCartUsd,
            bcvRate = bcvRate,
            onDismiss = { showCheckoutDialog = false },
            onConfirmSale = { customerName, customerPhone, customerId, paymentMethod, refNumber, notes, _, _, _ ->
                viewModel.processSale(
                    customerName = customerName,
                    customerPhone = customerPhone,
                    customerCedula = customerId,
                    paymentMethod = paymentMethod,
                    paymentReference = refNumber,
                    notes = notes
                )
                showCheckoutDialog = false
            }
        )
    }
}

@Composable
fun BentoDailyInspirationCard(verse: BiblicalVerse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BentoDarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "PALABRA DE BENDICIÓN PARA HOY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = verse.topic,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoMintContainer
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "«${verse.quote}»",
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "— ${verse.reference}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoMintContainer
                )
            }
        }
    }
}

@Composable
fun ProductPosCard(
    product: ProductEntity,
    bcvRate: Double,
    inCartQuantity: Int,
    onAddToCart: () -> Unit,
    onReduceQuantity: () -> Unit
) {
    val priceBs = product.salePriceUsd * bcvRate

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BentoCardBg),
        border = BorderStroke(1.dp, BentoBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge (Bento pill shape)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = BentoBackground,
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = when (product.category) {
                            "Víveres" -> "🍚"
                            "Charcutería" -> "🧀"
                            "Bebidas" -> "🥤"
                            "Café y Bebidas" -> "☕"
                            "Lácteos" -> "🥛"
                            "Limpieza" -> "🧼"
                            "Snacks" -> "🍪"
                            else -> "📦"
                        },
                        fontSize = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoDarkText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Dual Currency Pricing: USD + Bolívares BCV
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = TicketGenerator.formatUsd(product.salePriceUsd),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = BentoMintText
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = TicketGenerator.formatBs(priceBs),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoSlateLabel
                    )
                }

                // Profit and stock badge
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stock: ${product.stockQuantity}",
                        fontSize = 11.sp,
                        color = if (product.stockQuantity <= product.minStockAlert) Color.Red else BentoSlateLight
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BentoMintContainer
                    ) {
                        Text(
                            text = "+${String.format(java.util.Locale.US, "%.0f", product.profitMarginPercent)}% ganancia",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoMintText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Quick Add / Count control
            if (inCartQuantity > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(BentoBlueContainer, RoundedCornerShape(20.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onReduceQuantity,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Quitar", tint = BentoBlueDark, modifier = Modifier.size(15.dp))
                    }

                    Text(
                        text = inCartQuantity.toString(),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = BentoBlueDark,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar", tint = BentoBlueDark, modifier = Modifier.size(15.dp))
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BentoBluePrimary,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onAddToCart() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    bcvRate: Double,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BentoBackground),
        border = BorderStroke(1.dp, BentoBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoDarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${TicketGenerator.formatUsd(item.unitPriceUsd)} c/u • equiv. ${TicketGenerator.formatBs(item.unitPriceUsd * bcvRate)}",
                    fontSize = 11.sp,
                    color = BentoSlateLabel
                )
                Text(
                    text = "Total: ${TicketGenerator.formatUsd(item.totalUsd)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BentoMintText
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Menos", tint = BentoDarkText, modifier = Modifier.size(14.dp))
                }

                Text(
                    text = item.quantity.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoDarkText,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                IconButton(onClick = onIncrease, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Más", tint = BentoDarkText, modifier = Modifier.size(14.dp))
                }

                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.Red, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CheckoutDialog(
    cartItems: List<CartItem>,
    totalUsd: Double,
    bcvRate: Double,
    onDismiss: () -> Unit,
    onConfirmSale: (
        customerName: String,
        customerPhone: String,
        customerId: String,
        paymentMethod: PaymentMethod,
        refNumber: String,
        notes: String,
        amountReceivedUsd: Double,
        amountReceivedBs: Double,
        isPaid: Boolean
    ) -> Unit
) {
    val totalBs = totalUsd * bcvRate

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerId by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.PAGO_MOVIL) }
    var referenceNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BentoCardBg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PROCESAR COBRO Y VENTA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = BentoSlateLabel
                        )
                        Text(
                            text = "Datos del Cliente y Pago",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoDarkText
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = BentoSlateLabel)
                    }
                }
            }

            // Total Amount Banner in Bento format
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = BentoDarkCard,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MONTO TOTAL ($)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Text(
                                text = TicketGenerator.formatUsd(totalUsd),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "MONTO EN BS. (BCV)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoMintContainer
                            )
                            Text(
                                text = TicketGenerator.formatBs(totalBs),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoMintContainer
                            )
                        }
                    }
                }
            }

            // Required Customer Info
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "1. DATOS DEL CLIENTE (Obligatorio para Ticket)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = BentoSlateLabel
                    )

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = {
                            customerName = it
                            if (it.isNotBlank()) nameError = false
                        },
                        label = { Text("Nombre del Cliente * (Requerido)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BentoBluePrimary) },
                        isError = nameError,
                        supportingText = if (nameError) { { Text("El nombre es obligatorio para generar el ticket", color = Color.Red) } } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoBluePrimary,
                            unfocusedBorderColor = BentoBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_customer_name_input")
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("WhatsApp / Teléfono") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BentoMintText) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            placeholder = { Text("04141234567") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("checkout_customer_phone_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = customerId,
                            onValueChange = { customerId = it },
                            label = { Text("C.I. / RIF") },
                            placeholder = { Text("V-12345678") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("checkout_customer_id_input")
                        )
                    }
                }
            }

            // Venezuelan Payment Method Selection
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "2. MÉTODO DE PAGO VENEZUELA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = BentoSlateLabel
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PaymentMethod.values().forEach { method ->
                            val isSelected = selectedMethod == method
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) BentoBlueContainer else BentoBackground,
                                border = BorderStroke(1.dp, if (isSelected) BentoBluePrimary else BentoBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectedMethod = method }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = method.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) BentoBlueDark else BentoDarkText
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Reference Number (for Pago Móvil / Transferencia / Punto)
            if (selectedMethod == PaymentMethod.PAGO_MOVIL ||
                selectedMethod == PaymentMethod.TRANSFERENCIA ||
                selectedMethod == PaymentMethod.PUNTO_VENTA ||
                selectedMethod == PaymentMethod.BIOPAGO ||
                selectedMethod == PaymentMethod.MIXTO) {
                item {
                    OutlinedTextField(
                        value = referenceNumber,
                        onValueChange = { referenceNumber = it },
                        label = { Text("Número de Referencia (Últimos 4-6 dígitos)") },
                        leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null, tint = BentoBluePrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_ref_number_input")
                    )
                }
            }

            // Notes / Quincena Reminder info
            if (selectedMethod == PaymentMethod.CREDITO) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = BentoMintContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = BentoMintText, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Venta a Crédito / Fiado para Quincena (15 o 30)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoMintText
                                )
                            }
                            Text(
                                text = "Se registrará en el módulo de Créditos con recordatorio automático para la próxima quincena.",
                                fontSize = 11.sp,
                                color = BentoMintText
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas u observaciones de la venta") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Confirm Button
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        if (customerName.isBlank()) {
                            nameError = true
                            Toast.makeText(context, "Por favor ingresa el nombre del cliente para el ticket", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val isPaid = selectedMethod != PaymentMethod.CREDITO
                        onConfirmSale(
                            customerName.trim(),
                            customerPhone.trim(),
                            customerId.trim(),
                            selectedMethod,
                            referenceNumber.trim(),
                            notes.trim(),
                            totalUsd,
                            totalBs,
                            isPaid
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("confirm_sale_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FINALIZAR Y EMITIR TICKET",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
