package com.example.ui.inventory

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ProductEntity
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

@Composable
fun InventoryScreen(
    products: List<ProductEntity>,
    bcvRate: Double,
    onSaveProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
    onExportCsv: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }

    val categories = listOf("Todos", "Víveres", "Charcutería", "Bebidas", "Café y Bebidas", "Lácteos", "Limpieza", "Snacks")

    val filteredProducts = products.filter { p ->
        val matchesQuery = searchQuery.isBlank() || p.name.contains(searchQuery, ignoreCase = true) || p.code.contains(searchQuery, ignoreCase = true)
        val matchesCat = selectedCategory == "Todos" || p.category.equals(selectedCategory, ignoreCase = true)
        matchesQuery && matchesCat
    }

    val totalInventoryCost = products.sumOf { it.costPriceUsd * it.stockQuantity }
    val totalInventorySale = products.sumOf { it.salePriceUsd * it.stockQuantity }
    val expectedProfit = totalInventorySale - totalInventoryCost

    Scaffold(
        containerColor = BentoBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    productToEdit = null
                    showAddEditDialog = true
                },
                containerColor = BentoBluePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Producto")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BentoBackground)
        ) {
            // Bento Inventory Header
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
                                text = "CONTROL DE STOCK Y PRECIOS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = BentoSlateLabel
                            )
                            Text(
                                text = "Inventario & Productos",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = BentoDarkText
                            )
                        }

                        Button(
                            onClick = onExportCsv,
                            colors = ButtonDefaults.buttonColors(containerColor = BentoBlueDark),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("export_inventory_csv_button")
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

                    // Bento 2-Column Summary Row
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
                                Text("VALOR INVENTARIO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                                Text(
                                    text = TicketGenerator.formatUsd(totalInventorySale),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = TicketGenerator.formatBs(totalInventorySale * bcvRate),
                                    fontSize = 11.sp,
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
                                Text("GANANCIA ESTIMADA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BentoMintText.copy(alpha = 0.8f))
                                Text(
                                    text = "+${TicketGenerator.formatUsd(expectedProfit)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BentoMintText
                                )
                                Text(
                                    text = if (totalInventoryCost > 0) String.format(java.util.Locale.US, "%.1f%% margen global", (expectedProfit / totalInventoryCost) * 100) else "0% margen",
                                    fontSize = 11.sp,
                                    color = BentoMintText.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar & Filter Pills
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BentoCardBg)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por nombre o código...", fontSize = 13.sp, color = BentoSlateLabel) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BentoBluePrimary, modifier = Modifier.size(20.dp)) },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoBluePrimary,
                        unfocusedBorderColor = BentoBorder,
                        focusedContainerColor = BentoBackground,
                        unfocusedContainerColor = BentoBackground
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) BentoBlueDark else BentoBackground,
                            border = if (isSelected) null else BorderStroke(1.dp, BentoBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedCategory = category }
                        ) {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else BentoSlateLabel,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Product List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    InventoryProductCard(
                        product = product,
                        bcvRate = bcvRate,
                        onEdit = {
                            productToEdit = product
                            showAddEditDialog = true
                        },
                        onDelete = { onDeleteProduct(product) }
                    )
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditProductDialog(
            product = productToEdit,
            bcvRate = bcvRate,
            onDismiss = { showAddEditDialog = false },
            onSave = { savedProduct ->
                onSaveProduct(savedProduct)
                showAddEditDialog = false
            }
        )
    }
}

@Composable
fun InventoryProductCard(
    product: ProductEntity,
    bcvRate: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isLowStock = product.stockQuantity <= product.minStockAlert
    val priceBs = product.salePriceUsd * bcvRate

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
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BentoBlueContainer
                        ) {
                            Text(
                                text = product.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoBlueDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        if (product.code.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "#${product.code}",
                                fontSize = 11.sp,
                                color = BentoSlateLabel
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = product.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoDarkText
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = BentoBlueDark, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Costo: ${TicketGenerator.formatUsd(product.costPriceUsd)}",
                        fontSize = 11.sp,
                        color = BentoSlateLabel
                    )
                    Text(
                        text = "Venta: ${TicketGenerator.formatUsd(product.salePriceUsd)} (${TicketGenerator.formatBs(priceBs)})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoMintText
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BentoMintContainer
                    ) {
                        Text(
                            text = "+${String.format(java.util.Locale.US, "%.1f", product.profitMarginPercent)}% Ganancia",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoMintText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isLowStock) {
                            Icon(Icons.Default.Warning, contentDescription = "Bajo stock", tint = Color.Red, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "Stock: ${product.stockQuantity} unid.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowStock) Color.Red else BentoSlateLight
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditProductDialog(
    product: ProductEntity?,
    bcvRate: Double,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var code by remember { mutableStateOf(product?.code ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Víveres") }
    var costPriceText by remember { mutableStateOf(product?.costPriceUsd?.toString() ?: "1.00") }
    var salePriceText by remember { mutableStateOf(product?.salePriceUsd?.toString() ?: "1.40") }
    var stockText by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "20") }
    var minStockText by remember { mutableStateOf(product?.minStockAlert?.toString() ?: "5") }
    var description by remember { mutableStateOf(product?.description ?: "") }

    val cost = costPriceText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val sale = salePriceText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val profit = sale - cost
    val marginPercent = if (cost > 0) (profit / cost) * 100 else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BentoCardBg,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = if (product == null) "Nuevo Producto" else "Editar Producto",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BentoDarkText
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Producto *") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Código / SKU") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Categoría") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = costPriceText,
                        onValueChange = { costPriceText = it },
                        label = { Text("Precio Costo ($)") },
                        prefix = { Text("$ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = salePriceText,
                        onValueChange = { salePriceText = it },
                        label = { Text("Precio Venta ($)") },
                        prefix = { Text("$ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Profit preview card
                Surface(
                    color = BentoMintContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ganancia: +${TicketGenerator.formatUsd(profit)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoMintText
                        )
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f%% de Margen", marginPercent),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoMintText
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text("Stock Actual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { minStockText = it },
                        label = { Text("Alerta Mínima") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (Opcional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val newProduct = (product ?: ProductEntity(
                            name = name.trim(),
                            costPriceUsd = cost,
                            salePriceUsd = sale
                        )).copy(
                            name = name.trim(),
                            code = code.trim(),
                            category = category.trim().ifBlank { "General" },
                            costPriceUsd = cost,
                            salePriceUsd = sale,
                            stockQuantity = stockText.toIntOrNull() ?: 0,
                            minStockAlert = minStockText.toIntOrNull() ?: 5,
                            description = description.trim()
                        )
                        onSave(newProduct)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary)
            ) {
                Text("Guardar Producto", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(16.dp)) {
                Text("Cancelar", color = BentoSlateLabel)
            }
        }
    )
}
