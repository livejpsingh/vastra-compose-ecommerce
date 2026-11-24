@file:OptIn(ExperimentalMaterial3Api::class)

package com.vastra.store.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vastra.store.data.model.Product
import com.vastra.store.ui.ShopUiState
import com.vastra.store.ui.components.EmptyState
import com.vastra.store.ui.components.PriceBlock
import com.vastra.store.ui.components.ProductImage
import com.vastra.store.ui.components.QuantityStepper
import com.vastra.store.ui.components.RatingPill
import com.vastra.store.ui.components.SectionHeader
import com.vastra.store.ui.components.VastraTopBar
import com.vastra.store.util.Pricing
import com.vastra.store.util.categoryLabel
import com.vastra.store.util.formatInr
import com.vastra.store.util.sizesFor

@Composable
fun ProductDetailScreen(
    state: ShopUiState,
    productId: Int,
    onBack: () -> Unit,
    onAddToCart: (Product, String?, Int) -> Unit,
    onWishlistToggle: (Product) -> Unit,
    onGoToBag: () -> Unit
) {
    val product = state.productById(productId)

    Column(modifier = Modifier.fillMaxSize()) {
        VastraTopBar(
            title = product?.brandLabel ?: "Product",
            onBack = onBack,
            actions = {
                if (product != null) {
                    IconButton(onClick = { onWishlistToggle(product) }) {
                        Icon(
                            imageVector = if (state.isWishlisted(product.id)) Icons.Filled.Favorite
                            else Icons.Filled.FavoriteBorder,
                            contentDescription = if (state.isWishlisted(product.id)) {
                                "Remove from wishlist"
                            } else {
                                "Save to wishlist"
                            },
                            tint = if (state.isWishlisted(product.id)) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )

        if (product == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Filled.Storefront,
                    title = "This piece is no longer in the catalogue",
                    message = "Return to the shop and refresh the catalogue, then try again.",
                    actionLabel = "Go back",
                    onAction = onBack
                )
            }
            return@Column
        }

        val images = product.gallery
        val sizes = remember(product.id) { sizesFor(product.category) }
        var selectedSize by remember(product.id) { mutableStateOf(sizes.firstOrNull()) }
        var quantity by remember(product.id) { mutableStateOf(1) }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            val pagerState = rememberPagerState(pageCount = { images.size })

            Box {
                if (images.isEmpty()) {
                    ProductImage(
                        model = null,
                        contentDescription = product.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.85f)
                    )
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.85f)
                    ) { page ->
                        ProductImage(
                            model = images[page],
                            contentDescription = product.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
                if (images.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        repeat(images.size) { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                                    .background(
                                        color = if (pagerState.currentPage == index) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                        },
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
                if (images.size > 1) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${images.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                Text(
                    text = categoryLabel(product.category).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(6.dp))
                Text(text = product.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingPill(product.rating)
                    Spacer(Modifier.width(10.dp))
                    Surface(
                        color = if (product.inStock) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = if (product.inStock) "In stock" else "Out of stock",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (product.inStock) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                PriceBlock(product = product, large = true)

                if (sizes.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    SectionHeader("Select size")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sizes.forEach { size ->
                            FilterChip(
                                selected = selectedSize == size,
                                onClick = { selectedSize = size },
                                label = { Text(size) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("Quantity")
                    QuantityStepper(
                        quantity = quantity,
                        onDecrease = { quantity = (quantity - 1).coerceAtLeast(1) },
                        onIncrease = {
                            quantity = (quantity + 1).coerceAtMost(Pricing.MAX_QUANTITY_PER_LINE)
                        },
                        canIncrease = quantity < Pricing.MAX_QUANTITY_PER_LINE
                    )
                }

                Spacer(Modifier.height(16.dp))
                SectionHeader("Description")
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(18.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Free delivery over ${formatInr(Pricing.FREE_SHIPPING_INR)}",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = "Ships in 3–5 days · 7-day easy returns",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.padding(end = 12.dp)) {
                    Text(
                        text = "YOUR PRICE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatInr(product.finalPrice * quantity),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Button(
                    onClick = { onAddToCart(product, selectedSize, quantity) },
                    enabled = product.inStock,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        text = if (product.inStock) "Add to bag" else "Out of stock",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.width(10.dp))
                OutlinedButton(
                    onClick = onGoToBag,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = "Open bag",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
