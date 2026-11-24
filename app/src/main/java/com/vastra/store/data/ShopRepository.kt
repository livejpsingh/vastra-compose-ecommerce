package com.vastra.store.data

import com.vastra.store.data.local.ShopStore
import com.vastra.store.data.model.Product
import com.vastra.store.data.remote.ShopApi
import com.vastra.store.util.Pricing

/**
 * Owns the catalogue: fetches over network with offline persistence fallback,
 * in-memory caching, currency mapping to INR, and narrowing to fashion categories.
 */
class ShopRepository(
    private val api: ShopApi,
    private val store: ShopStore
) {

    @Volatile
    private var cache: List<Product>? = null

    suspend fun products(forceRefresh: Boolean = false): List<Product> {
        cache?.takeIf { !forceRefresh }?.let { return it }

        // If not forcing refresh, try loading from local disk cache first
        if (!forceRefresh) {
            val localCache = store.getCatalogueCache()
            if (localCache.isNotEmpty()) {
                cache = localCache
                return localCache
            }
        }

        return try {
            val remote = api.getAllProducts().products
                .filter { it.title.isNotBlank() }
                .map(::toRupees)

            // Keep the store on-topic. If the API ever renames its categories the
            // filter would empty the catalogue, so fall back to everything.
            val fashion = remote.filter { it.category in FASHION_CATEGORIES }
            val result = fashion.ifEmpty { remote }

            cache = result
            store.saveCatalogueCache(result)
            result
        } catch (t: Throwable) {
            // Gracefully fall back to local disk cache if network fails
            val fallback = store.getCatalogueCache()
            if (fallback.isNotEmpty()) {
                cache = fallback
                fallback
            } else {
                throw t
            }
        }
    }

    private fun toRupees(product: Product): Product =
        product.copy(price = product.price * Pricing.USD_TO_INR)

    companion object {
        val FASHION_CATEGORIES = setOf(
            "mens-shirts",
            "mens-shoes",
            "mens-watches",
            "womens-bags",
            "womens-dresses",
            "womens-jewellery",
            "womens-shoes",
            "womens-watches",
            "tops"
        )
    }
}
