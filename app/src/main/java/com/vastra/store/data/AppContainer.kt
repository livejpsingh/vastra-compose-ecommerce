package com.vastra.store.data

import android.content.Context
import com.vastra.store.data.local.ShopStore
import com.vastra.store.data.remote.Network

/**
 * Manual dependency container, created once by [com.vastra.store.VastraApplication].
 * Provides singleton store and repository across the application lifecycle.
 */
class AppContainer(context: Context) {

    val store: ShopStore = ShopStore(context.applicationContext)
    val repository: ShopRepository = ShopRepository(Network.api, store)
}
