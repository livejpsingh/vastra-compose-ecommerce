@file:OptIn(ExperimentalMaterial3Api::class)

package com.vastra.store.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vastra.store.data.model.Product
import com.vastra.store.ui.ShopUiState
import com.vastra.store.ui.components.EmptyState
import com.vastra.store.ui.components.ProductCard
import com.vastra.store.ui.components.VastraTopBar

@Composable
fun WishlistScreen(
    state: ShopUiState,
    onProductClick: (Product) -> Unit,
    onWishlistToggle: (Product) -> Unit,
    onMoveToBag: (Product) -> Unit,
    onBrowseShop: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        VastraTopBar(title = "Wishlist")

        if (state.wishlist.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Filled.FavoriteBorder,
                    title = "Nothing saved yet",
                    message = "Tap the heart on any piece to keep it here for later.",
                    actionLabel = "Browse the shop",
                    onAction = onBrowseShop
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved for later",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${state.wishlist.size} ${if (state.wishlist.size == 1) "piece" else "pieces"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.wishlist, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        wishlisted = true,
                        onOpen = { onProductClick(product) },
                        onWishlistToggle = { onWishlistToggle(product) },
                        onQuickAdd = { onMoveToBag(product) }
                    )
                }
            }
        }
    }
}
