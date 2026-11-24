@file:OptIn(ExperimentalMaterial3Api::class)

package com.vastra.store.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vastra.store.ui.screens.CartScreen
import com.vastra.store.ui.screens.CatalogueScreen
import com.vastra.store.ui.screens.CheckoutScreen
import com.vastra.store.ui.screens.OrdersScreen
import com.vastra.store.ui.screens.ProductDetailScreen
import com.vastra.store.ui.screens.ProfileScreen
import com.vastra.store.ui.screens.WishlistScreen
import com.vastra.store.util.sizesFor
import kotlinx.coroutines.launch

object Routes {
    const val CATALOGUE = "catalogue"
    const val WISHLIST = "wishlist"
    const val CART = "cart"
    const val ORDERS = "orders"
    const val PROFILE = "profile"
    const val CHECKOUT = "checkout"

    const val PRODUCT_ARG = "productId"
    const val PRODUCT = "product/{$PRODUCT_ARG}"

    fun product(id: Int): String = "product/$id"
}

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem(Routes.CATALOGUE, "Shop", Icons.Filled.Home),
    TabItem(Routes.WISHLIST, "Wishlist", Icons.Filled.FavoriteBorder),
    TabItem(Routes.CART, "Bag", Icons.Filled.ShoppingCart),
    TabItem(Routes.ORDERS, "Orders", Icons.Filled.ReceiptLong),
    TabItem(Routes.PROFILE, "Profile", Icons.Filled.Person)
)

@Composable
fun VastraApp(viewModel: ShopViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = tabs.any { it.route == currentRoute }

    LaunchedEffect(state.lastOrderId) {
        val orderId = state.lastOrderId ?: return@LaunchedEffect
        viewModel.consumeLastOrder()
        snackbarHostState.showSnackbar("Order $orderId placed. Thank you!")
    }

    fun toast(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    tabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) navController.navigateToTab(tab.route)
                            },
                            icon = {
                                if (tab.route == Routes.CART && state.cartCount > 0) {
                                    BadgedBox(badge = { Badge { Text(state.cartCount.toString()) } }) {
                                        Icon(tab.icon, contentDescription = tab.label)
                                    }
                                } else {
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.CATALOGUE,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.CATALOGUE) {
                CatalogueScreen(
                    state = state,
                    onQueryChange = viewModel::onQueryChange,
                    onCategorySelect = viewModel::onCategorySelect,
                    onMinRatingChange = viewModel::onMinRatingChange,
                    onMaxPriceChange = viewModel::onMaxPriceChange,
                    onInStockToggle = viewModel::onInStockToggle,
                    onSortChange = viewModel::onSortChange,
                    onClearFilters = viewModel::clearFilters,
                    onRetry = viewModel::refresh,
                    onProductClick = { product -> navController.navigate(Routes.product(product.id)) },
                    onWishlistToggle = { product ->
                        val wasSaved = state.isWishlisted(product.id)
                        viewModel.toggleWishlist(product)
                        toast(if (wasSaved) "Removed from wishlist" else "Saved to wishlist")
                    },
                    onQuickAdd = { product ->
                        if (sizesFor(product.category).isEmpty()) {
                            viewModel.addToCart(product, size = null)
                            toast("${product.title} added to bag")
                        } else {
                            navController.navigate(Routes.product(product.id))
                        }
                    }
                )
            }

            composable(Routes.PRODUCT, arguments = listOf(navArgument(Routes.PRODUCT_ARG) { type = NavType.IntType })) { entry ->
                val productId = entry.arguments?.getInt(Routes.PRODUCT_ARG) ?: -1
                ProductDetailScreen(
                    state = state,
                    productId = productId,
                    onBack = { navController.popBackStack() },
                    onAddToCart = { product, size, quantity ->
                        viewModel.addToCart(product, size, quantity)
                        toast("Added to bag")
                    },
                    onWishlistToggle = { product ->
                        val wasSaved = state.isWishlisted(product.id)
                        viewModel.toggleWishlist(product)
                        toast(if (wasSaved) "Removed from wishlist" else "Saved to wishlist")
                    },
                    onGoToBag = { navController.navigateToTab(Routes.CART) }
                )
            }

            composable(Routes.WISHLIST) {
                WishlistScreen(
                    state = state,
                    onProductClick = { product -> navController.navigate(Routes.product(product.id)) },
                    onWishlistToggle = { product ->
                        viewModel.toggleWishlist(product)
                        toast("Removed from wishlist")
                    },
                    onMoveToBag = { product ->
                        if (!product.inStock) {
                            toast("This piece is currently out of stock")
                            return@WishlistScreen
                        }
                        val sizes = sizesFor(product.category)
                        if (sizes.isEmpty()) {
                            viewModel.addToCart(product, size = null)
                            toast("${product.title} added to bag")
                        } else {
                            navController.navigate(Routes.product(product.id))
                        }
                    },
                    onBrowseShop = { navController.navigateToTab(Routes.CATALOGUE) }
                )
            }

            composable(Routes.CART) {
                CartScreen(
                    state = state,
                    onQuantityChange = viewModel::setQuantity,
                    onRemove = viewModel::removeFromCart,
                    onCheckout = { navController.navigate(Routes.CHECKOUT) },
                    onBrowseShop = { navController.navigateToTab(Routes.CATALOGUE) }
                )
            }

            composable(Routes.CHECKOUT) {
                CheckoutScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onPlaceOrder = viewModel::placeOrder,
                    onOrderPlaced = {
                        navController.navigate(Routes.ORDERS) {
                            popUpTo(Routes.CART) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onEditBag = {
                        navController.navigate(Routes.CART) {
                            popUpTo(Routes.CHECKOUT) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.ORDERS) {
                OrdersScreen(
                    orders = state.orders,
                    onBrowseShop = { navController.navigateToTab(Routes.CATALOGUE) }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    state = state,
                    onSaveProfile = viewModel::saveProfile,
                    onSaveAddress = viewModel::saveAddress,
                    onClearData = viewModel::clearLocalData,
                    onOpenOrders = { navController.navigateToTab(Routes.ORDERS) }
                )
            }
        }
    }
}

private fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
