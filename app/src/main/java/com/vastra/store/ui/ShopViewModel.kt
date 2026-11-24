package com.vastra.store.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vastra.store.data.ShopRepository
import com.vastra.store.data.local.Profile
import com.vastra.store.data.local.ShopStore
import com.vastra.store.data.model.Address
import com.vastra.store.data.model.CartLine
import com.vastra.store.data.model.Order
import com.vastra.store.data.model.Product
import com.vastra.store.util.Pricing
import com.vastra.store.util.categoryLabel
import com.vastra.store.util.shippingFor
import com.vastra.store.util.validateAddress
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

enum class SortOption(val label: String) {
    RECOMMENDED("Recommended"),
    PRICE_LOW_HIGH("Price: low to high"),
    PRICE_HIGH_LOW("Price: high to low"),
    RATING("Top rated"),
    DISCOUNT("Biggest discount")
}

data class ShopUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val isPlacingOrder: Boolean = false,
    val error: String? = null,
    val products: List<Product> = emptyList(),
    val query: String = "",
    val selectedCategory: String? = null,
    val minRating: Float = 0f,
    val maxPrice: Float = 0f,
    val onlyInStock: Boolean = false,
    val sort: SortOption = SortOption.RECOMMENDED,
    val cart: List<CartLine> = emptyList(),
    val wishlist: List<Product> = emptyList(),
    val orders: List<Order> = emptyList(),
    val profile: Profile = Profile(),
    val savedAddress: Address = Address(),
    val lastOrderId: String? = null
) {

    val categories: List<String>
        get() = products.map { it.category }.distinct().sortedBy { categoryLabel(it) }

    val priceCeiling: Float
        get() = products.maxOfOrNull { it.finalPrice }?.toFloat()?.coerceAtLeast(500f) ?: 2000f

    val visibleProducts: List<Product>
        get() {
            val needle = query.trim().lowercase()
            val filtered = products.filter { product ->
                val matchesQuery = needle.isEmpty() ||
                    product.title.lowercase().contains(needle) ||
                    product.brandLabel.lowercase().contains(needle) ||
                    categoryLabel(product.category).lowercase().contains(needle)
                val matchesCategory = selectedCategory == null || product.category == selectedCategory
                val matchesRating = product.rating >= minRating.toDouble()
                val matchesPrice = maxPrice <= 0f || product.finalPrice <= maxPrice
                val matchesStock = !onlyInStock || product.inStock
                matchesQuery && matchesCategory && matchesRating && matchesPrice && matchesStock
            }
            return when (sort) {
                SortOption.RECOMMENDED -> filtered.sortedWith(
                    compareByDescending<Product> { it.rating }
                        .thenByDescending { it.discountPercentage }
                )
                SortOption.PRICE_LOW_HIGH -> filtered.sortedBy { it.finalPrice }
                SortOption.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.finalPrice }
                SortOption.RATING -> filtered.sortedByDescending { it.rating }
                SortOption.DISCOUNT -> filtered.sortedByDescending { it.discountPercentage }
            }
        }

    val activeFilterCount: Int
        get() = listOf(
            selectedCategory != null,
            minRating > 0f,
            maxPrice > 0f,
            onlyInStock
        ).count { it }

    val cartCount: Int get() = cart.sumOf { it.quantity }
    val subtotal: Double get() = cart.sumOf { it.lineTotal }
    val shipping: Double get() = shippingFor(subtotal)
    val total: Double get() = subtotal + shipping
    val hasShopped: Boolean get() = orders.isNotEmpty()
    val hasOutOfStockInCart: Boolean get() = cart.any { !it.product.inStock }

    fun productById(id: Int): Product? =
        products.firstOrNull { it.id == id } ?: wishlist.firstOrNull { it.id == id }

    fun isWishlisted(id: Int): Boolean = wishlist.any { it.id == id }
}

class ShopViewModel(
    private val repository: ShopRepository,
    private val store: ShopStore
) : ViewModel() {

    private val _state = MutableStateFlow(ShopUiState())
    val state: StateFlow<ShopUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        observeStoredData()
        load(force = false)
    }

    // ---------------------------------------------------------------- catalogue

    fun refresh() = load(force = true)

    private fun load(force: Boolean) {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    loading = it.products.isEmpty(),
                    refreshing = it.products.isNotEmpty(),
                    error = null
                )
            }
            try {
                val products = repository.products(forceRefresh = force)
                _state.update { it.copy(loading = false, refreshing = false, products = products) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                _state.update {
                    it.copy(loading = false, refreshing = false, error = error.toUserMessage())
                }
            }
        }
    }

    fun onQueryChange(query: String) = _state.update { it.copy(query = query) }

    fun onCategorySelect(category: String?) = _state.update { it.copy(selectedCategory = category) }

    fun onMinRatingChange(rating: Float) = _state.update { it.copy(minRating = rating) }

    fun onMaxPriceChange(price: Float) = _state.update { it.copy(maxPrice = price) }

    fun onInStockToggle(onlyInStock: Boolean) = _state.update { it.copy(onlyInStock = onlyInStock) }

    fun onSortChange(sort: SortOption) = _state.update { it.copy(sort = sort) }

    fun clearFilters() = _state.update {
        it.copy(selectedCategory = null, minRating = 0f, maxPrice = 0f, onlyInStock = false)
    }

    // --------------------------------------------------------------------- bag

    fun addToCart(product: Product, size: String?, quantity: Int = 1) {
        if (!product.inStock) return
        val safeQuantity = quantity.coerceIn(1, Pricing.MAX_QUANTITY_PER_LINE)
        val updated = _state.value.cart.toMutableList()
        val index = updated.indexOfFirst { it.product.id == product.id && it.size == size }
        if (index >= 0) {
            val line = updated[index]
            updated[index] = line.copy(
                quantity = (line.quantity + safeQuantity).coerceAtMost(Pricing.MAX_QUANTITY_PER_LINE)
            )
        } else {
            updated.add(CartLine(product = product, size = size, quantity = safeQuantity))
        }
        persistCart(updated)
    }

    fun setQuantity(productId: Int, size: String?, quantity: Int) {
        val updated = _state.value.cart.mapNotNull { line ->
            if (line.product.id == productId && line.size == size) {
                if (quantity <= 0) null
                else line.copy(quantity = quantity.coerceAtMost(Pricing.MAX_QUANTITY_PER_LINE))
            } else {
                line
            }
        }
        persistCart(updated)
    }

    fun removeFromCart(productId: Int, size: String?) {
        persistCart(_state.value.cart.filterNot { it.product.id == productId && it.size == size })
    }

    private fun persistCart(lines: List<CartLine>) {
        viewModelScope.launch { store.saveCart(lines) }
    }

    // ---------------------------------------------------------------- wishlist

    fun toggleWishlist(product: Product) {
        val current = _state.value.wishlist
        val updated = if (current.any { it.id == product.id }) {
            current.filterNot { it.id == product.id }
        } else {
            current + product
        }
        viewModelScope.launch { store.saveWishlist(updated) }
    }

    // ------------------------------------------------------------------ orders

    /** Asynchronously validates and places the order, protecting against duplicate taps. */
    fun placeOrder(
        address: Address,
        paymentMethod: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (_state.value.isPlacingOrder) return

        val lines = _state.value.cart
        if (lines.isEmpty()) {
            onError("Your bag is empty.")
            return
        }

        val sanitizedAddress = address.copy(
            fullName = address.fullName.trim(),
            phone = address.phone.trim().filter { it.isDigit() },
            line1 = address.line1.trim(),
            city = address.city.trim(),
            state = address.state.trim(),
            pincode = address.pincode.trim().filter { it.isDigit() }
        )

        validateAddress(sanitizedAddress)?.let {
            onError(it)
            return
        }

        val outOfStockItems = lines.filter { !it.product.inStock }
        if (outOfStockItems.isNotEmpty()) {
            val names = outOfStockItems.joinToString { it.product.title }
            onError("Some items in your bag are out of stock: $names")
            return
        }

        _state.update { it.copy(isPlacingOrder = true) }

        val subtotal = lines.sumOf { it.lineTotal }
        val shipping = shippingFor(subtotal)
        val order = Order(
            id = newOrderId(),
            placedAt = System.currentTimeMillis(),
            lines = lines,
            subtotal = subtotal,
            shipping = shipping,
            total = subtotal + shipping,
            address = sanitizedAddress,
            paymentMethod = paymentMethod,
            status = "Confirmed"
        )
        val existing = _state.value.orders

        viewModelScope.launch {
            try {
                store.saveOrders(existing + order)
                store.saveAddress(sanitizedAddress)
                store.saveCart(emptyList())
                _state.update {
                    it.copy(
                        isPlacingOrder = false,
                        lastOrderId = order.id
                    )
                }
                onSuccess(order.id)
            } catch (t: Throwable) {
                _state.update { it.copy(isPlacingOrder = false) }
                onError("Failed to place order: ${t.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun consumeLastOrder() = _state.update { it.copy(lastOrderId = null) }

    private fun newOrderId(): String {
        val suffix = (System.currentTimeMillis() % 100_000_000L).toString().padStart(8, '0')
        return "VS$suffix"
    }

    // ------------------------------------------------------------ profile/data

    fun saveProfile(profile: Profile) {
        val sanitized = profile.copy(
            name = profile.name.trim(),
            email = profile.email.trim(),
            phone = profile.phone.trim().filter { it.isDigit() }
        )
        viewModelScope.launch { store.saveProfile(sanitized) }
    }

    fun saveAddress(address: Address) {
        val sanitized = address.copy(
            fullName = address.fullName.trim(),
            phone = address.phone.trim().filter { it.isDigit() },
            line1 = address.line1.trim(),
            city = address.city.trim(),
            state = address.state.trim(),
            pincode = address.pincode.trim().filter { it.isDigit() }
        )
        viewModelScope.launch { store.saveAddress(sanitized) }
    }

    fun clearLocalData() {
        viewModelScope.launch { store.clearAll() }
    }

    // ------------------------------------------------------------------ wiring

    private fun observeStoredData() {
        viewModelScope.launch {
            store.cart.collect { lines -> _state.update { it.copy(cart = lines) } }
        }
        viewModelScope.launch {
            store.wishlist.collect { products -> _state.update { it.copy(wishlist = products) } }
        }
        viewModelScope.launch {
            store.orders.collect { orders ->
                _state.update { it.copy(orders = orders.sortedByDescending { order -> order.placedAt }) }
            }
        }
        viewModelScope.launch {
            store.profile.collect { profile -> _state.update { it.copy(profile = profile) } }
        }
        viewModelScope.launch {
            store.address.collect { address -> _state.update { it.copy(savedAddress = address) } }
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is UnknownHostException, is ConnectException, is SocketTimeoutException ->
            "Could not reach the catalogue. Check your internet connection and try again."
        is HttpException ->
            "The catalogue service replied with error ${code()}. Please try again in a moment."
        else ->
            "Something went wrong while loading products: ${message ?: this::class.java.simpleName}"
    }
}

class ShopViewModelFactory(
    private val repository: ShopRepository,
    private val store: ShopStore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ShopViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return ShopViewModel(repository, store) as T
    }
}
