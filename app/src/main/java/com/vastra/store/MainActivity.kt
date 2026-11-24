package com.vastra.store

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vastra.store.ui.ShopViewModel
import com.vastra.store.ui.ShopViewModelFactory
import com.vastra.store.ui.VastraApp
import com.vastra.store.ui.theme.VastraTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as VastraApplication).container
        val factory = ShopViewModelFactory(container.repository, container.store)

        setContent {
            VastraTheme {
                val viewModel: ShopViewModel = viewModel(factory = factory)
                VastraApp(viewModel = viewModel)
            }
        }
    }
}
