package com.vastra.store.data.remote

import com.vastra.store.data.model.ProductsResponse
import retrofit2.http.GET

/**
 * DummyJSON catalogue. `limit=0` returns the whole catalogue in one call so that
 * search, filters and sorting run locally and keep working on a flaky connection.
 * Docs: https://dummyjson.com/docs/products
 */
interface ShopApi {

    @GET("products?limit=0")
    suspend fun getAllProducts(): ProductsResponse
}
