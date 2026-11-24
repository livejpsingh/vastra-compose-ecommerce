@file:OptIn(ExperimentalMaterial3Api::class)

package com.vastra.store.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vastra.store.data.model.CartLine
import com.vastra.store.ui.ShopUiState
import com.vastra.store.ui.components.EmptyState
import com.vastra.store.ui.components.QuantityStepper
import com.vastra.store.ui.components.SummaryRow
import com.vastra.store.ui.components.VastraTopBar
import com.vastra.store.util.Pricing
import com.vastra.store.util.formatInr
import com.vastra.store.util.formatInrExact
import com.vastra.store.util.freeShippingShortfall

@Composable
fun CartScreen(
    state: ShopUiState,
    onQuantityChange: (Int, String?, Int) -> Unit,
    onRemove: (Int, String?) -> Unit,
    onCheckout: () -> Unit,
    onBrowseShop: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        VastraTopBar(title = "Your bag")

        if (state.cart.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Filled.ShoppingCart,
                    title = "Your bag is empty",
                    message = "Pieces you add will show up here, ready for checkout.",
                    actionLabel = "Start shopping",
                    onAction = onBrowseShop
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.cart, key = { "${it.product.id}-${it.size ?: "one"}" }) { line ->
                    CartRow(
                        line = line,
                        onQuantityChange = onQuantityChange,
                        onRemove = onRemove
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                    if (state.hasOutOfStockInCart) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Text(
                                text = "Some pieces in your bag are out of stock. Please remove them before checkout.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    val shortfall = freeShippingShortfall(state.subtotal)
                    if (shortfall > 0.0) {
                        Text(
                            text = "Add ${formatInr(shortfall)} more for free delivery",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                    SummaryRow(
                        label = "Subtotal (${state.cartCount} ${if (state.cartCount == 1) "item" else "items"})",
                        value = formatInrExact(state.subtotal)
                    )
                    SummaryRow(
                        label = "Delivery",
                        value = if (state.shipping == 0.0) "Free" else formatInrExact(state.shipping)
                    )
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    SummaryRow(
                        label = "Total",
                        value = formatInrExact(state.total),
                        emphasise = true
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onCheckout,
                        enabled = !state.hasOutOfStockInCart,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Proceed to checkout", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CartRow(
    line: CartLine,
    onQuantityChange: (Int, String?, Int) -> Unit,
    onRemove: (Int, String?) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            AsyncImage(
                model = line.product.thumbnail,
                contentDescription = line.product.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(78.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = line.product.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (line.size != null) {
                        Text(
                            text = "Size ${line.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!line.product.inStock) {
                        if (line.size != null) Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Out of stock",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatInr(line.lineTotal),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QuantityStepper(
                        quantity = line.quantity,
                        onDecrease = {
                            onQuantityChange(line.product.id, line.size, line.quantity - 1)
                        },
                        onIncrease = {
                            onQuantityChange(line.product.id, line.size, line.quantity + 1)
                        },
                        canIncrease = line.product.inStock && line.quantity < Pricing.MAX_QUANTITY_PER_LINE
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { onRemove(line.product.id, line.size) }) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Remove ${line.product.title}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
