package com.vastra.store.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vastra.store.data.model.Address
import com.vastra.store.data.model.CartLine
import com.vastra.store.data.model.Order
import com.vastra.store.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.lang.reflect.Type

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vastra_store")

/** Locally stored shopper details. No account or password is involved. */
data class Profile(
    val name: String = "",
    val email: String = "",
    val phone: String = ""
)

/**
 * Everything the shopper creates lives on the device: bag, wishlist, orders,
 * profile, the last delivery address, and offline catalogue cache.
 */
class ShopStore(context: Context) {

    private val dataStore = context.dataStore
    private val gson = Gson()

    private val cartType: Type = object : TypeToken<List<CartLine>>() {}.type
    private val wishlistType: Type = object : TypeToken<List<Product>>() {}.type
    private val ordersType: Type = object : TypeToken<List<Order>>() {}.type
    private val profileType: Type = object : TypeToken<Profile>() {}.type
    private val addressType: Type = object : TypeToken<Address>() {}.type
    private val catalogueType: Type = object : TypeToken<List<Product>>() {}.type

    val cart: Flow<List<CartLine>> = dataStore.data.map { prefs ->
        decode<List<CartLine>>(prefs[Keys.CART], cartType) ?: emptyList()
    }

    val wishlist: Flow<List<Product>> = dataStore.data.map { prefs ->
        decode<List<Product>>(prefs[Keys.WISHLIST], wishlistType) ?: emptyList()
    }

    val orders: Flow<List<Order>> = dataStore.data.map { prefs ->
        decode<List<Order>>(prefs[Keys.ORDERS], ordersType) ?: emptyList()
    }

    val profile: Flow<Profile> = dataStore.data.map { prefs ->
        decode<Profile>(prefs[Keys.PROFILE], profileType) ?: Profile()
    }

    val address: Flow<Address> = dataStore.data.map { prefs ->
        decode<Address>(prefs[Keys.ADDRESS], addressType) ?: Address()
    }

    suspend fun saveCart(lines: List<CartLine>) {
        dataStore.edit { prefs -> prefs[Keys.CART] = gson.toJson(lines) }
    }

    suspend fun saveWishlist(products: List<Product>) {
        dataStore.edit { prefs -> prefs[Keys.WISHLIST] = gson.toJson(products) }
    }

    suspend fun saveOrders(orders: List<Order>) {
        dataStore.edit { prefs -> prefs[Keys.ORDERS] = gson.toJson(orders) }
    }

    suspend fun saveProfile(profile: Profile) {
        dataStore.edit { prefs -> prefs[Keys.PROFILE] = gson.toJson(profile) }
    }

    suspend fun saveAddress(address: Address) {
        dataStore.edit { prefs -> prefs[Keys.ADDRESS] = gson.toJson(address) }
    }

    suspend fun saveCatalogueCache(products: List<Product>) {
        dataStore.edit { prefs -> prefs[Keys.CATALOGUE] = gson.toJson(products) }
    }

    suspend fun getCatalogueCache(): List<Product> = runCatching {
        val prefs = dataStore.data.first()
        decode<List<Product>>(prefs[Keys.CATALOGUE], catalogueType) ?: emptyList()
    }.getOrDefault(emptyList())

    suspend fun clearAll() {
        dataStore.edit { prefs -> prefs.clear() }
    }

    private fun <T> decode(raw: String?, type: Type): T? =
        raw?.let { json -> runCatching { gson.fromJson<T>(json, type) }.getOrNull() }

    private object Keys {
        val CART = stringPreferencesKey("cart")
        val WISHLIST = stringPreferencesKey("wishlist")
        val ORDERS = stringPreferencesKey("orders")
        val PROFILE = stringPreferencesKey("profile")
        val ADDRESS = stringPreferencesKey("address")
        val CATALOGUE = stringPreferencesKey("catalogue")
    }
}
