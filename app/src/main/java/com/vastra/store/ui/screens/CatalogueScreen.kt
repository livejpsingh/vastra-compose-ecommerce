@file:OptIn(ExperimentalMaterial3Api::class)

package com.vastra.store.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vastra.store.data.model.Product
import com.vastra.store.ui.ShopUiState
import com.vastra.store.ui.SortOption
import com.vastra.store.ui.components.EmptyState
import com.vastra.store.ui.components.ErrorState
import com.vastra.store.ui.components.LoadingState
import com.vastra.store.ui.components.ProductCard
import com.vastra.store.ui.components.SectionHeader
import com.vastra.store.ui.components.VastraTopBar
import com.vastra.store.util.categoryLabel
import com.vastra.store.util.formatInr

@Composable
fun CatalogueScreen(
    state: ShopUiState,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (String?) -> Unit,
    onMinRatingChange: (Float) -> Unit,
    onMaxPriceChange: (Float) -> Unit,
    onInStockToggle: (Boolean) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onClearFilters: () -> Unit,
    onRetry: () -> Unit,
    onProductClick: (Product) -> Unit,
    onWishlistToggle: (Product) -> Unit,
    onQuickAdd: (Product) -> Unit
) {
    var showFilters by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        VastraTopBar(
            title = "Vastra",
            actions = {
                IconButton(onClick = onRetry) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh catalogue")
                }
            }
        )

        if (state.refreshing) {
            androidx.compose.material3.LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            placeholder = { Text("Search shirts, dresses, bags…") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(50)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedCategory == null,
                        onClick = { onCategorySelect(null) },
                        label = { Text("All") }
                    )
                }
                state.categories.forEach { category ->
                    item(key = category) {
                        FilterChip(
                            selected = state.selectedCategory == category,
                            onClick = {
                                onCategorySelect(
                                    if (state.selectedCategory == category) null else category
                                )
                            },
                            label = { Text(categoryLabel(category)) }
                        )
                    }
                }
            }
            Spacer(Modifier.width(6.dp))
            FilledTonalIconButton(onClick = { showFilters = true }) {
                BadgedBox(
                    badge = {
                        if (state.activeFilterCount > 0) {
                            Badge { Text(state.activeFilterCount.toString()) }
                        }
                    }
                ) {
                    Icon(Icons.Filled.Tune, contentDescription = "Filters and sorting")
                }
            }
        }

        Text(
            text = buildString {
                append(state.visibleProducts.size)
                append(" pieces")
                if (state.sort != SortOption.RECOMMENDED) {
                    append(" · ")
                    append(state.sort.label)
                }
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 2.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                state.loading && state.products.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingState()
                    }
                }

                state.error != null && state.products.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        ErrorState(message = state.error.orEmpty(), onRetry = onRetry)
                    }
                }

                state.visibleProducts.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            icon = Icons.Filled.Search,
                            title = "Nothing matches those filters",
                            message = "Try a different search, or clear the filters to see the whole collection.",
                            actionLabel = "Clear filters",
                            onAction = {
                                onQueryChange("")
                                onClearFilters()
                            }
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.visibleProducts, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                wishlisted = state.isWishlisted(product.id),
                                onOpen = { onProductClick(product) },
                                onWishlistToggle = { onWishlistToggle(product) },
                                onQuickAdd = { onQuickAdd(product) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilters) {
        FilterSheet(
            state = state,
            onMinRatingChange = onMinRatingChange,
            onMaxPriceChange = onMaxPriceChange,
            onInStockToggle = onInStockToggle,
            onSortChange = onSortChange,
            onClearFilters = onClearFilters,
            onDismiss = { showFilters = false }
        )
    }
}

@Composable
private fun FilterSheet(
    state: ShopUiState,
    onMinRatingChange: (Float) -> Unit,
    onMaxPriceChange: (Float) -> Unit,
    onInStockToggle: (Boolean) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val ceiling = state.priceCeiling
    val sliderValue = if (state.maxPrice <= 0f) 0f else state.maxPrice.coerceIn(0f, ceiling)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filters & sorting", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onClearFilters) { Text("Clear all") }
            }

            SectionHeader("Sort by")
            Column {
                SortOption.entries.forEach { option ->
                    FilterChip(
                        selected = state.sort == option,
                        onClick = { onSortChange(option) },
                        label = { Text(option.label) },
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            SectionHeader("Customer rating")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0f, 3f, 4f, 4.5f).forEach { threshold ->
                    FilterChip(
                        selected = state.minRating == threshold,
                        onClick = { onMinRatingChange(threshold) },
                        label = {
                            Text(if (threshold == 0f) "Any" else "$threshold★ & up")
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("Maximum price")
            Text(
                text = if (state.maxPrice <= 0f) "Any price" else "Up to ${formatInr(state.maxPrice.toDouble())}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = sliderValue,
                onValueChange = { onMaxPriceChange(it) },
                valueRange = 0f..ceiling
            )

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("In stock only", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = state.onlyInStock, onCheckedChange = onInStockToggle)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Show ${state.visibleProducts.size} pieces",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
