package com.vastra.store

import com.vastra.store.data.model.CartLine
import com.vastra.store.data.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductModelTest {

    @Test
    fun testFinalPriceCalculation() {
        val product = Product(
            id = 1,
            title = "Linen Shirt",
            price = 1000.0,
            discountPercentage = 20.0,
            stock = 10
        )
        // 1000 - 20% = 800.0
        assertEquals(800.0, product.finalPrice, 0.001)
        assertTrue(product.inStock)
    }

    @Test
    fun testOutOfStockFlag() {
        val outOfStock = Product(id = 2, title = "Leather Boots", price = 2500.0, stock = 0)
        assertFalse(outOfStock.inStock)
    }

    @Test
    fun testCartLineTotal() {
        val product = Product(
            id = 3,
            title = "Cotton Dress",
            price = 2000.0,
            discountPercentage = 10.0,
            stock = 5
        )
        val line = CartLine(product = product, size = "M", quantity = 3)
        // final price = 1800.0, 3 x 1800 = 5400.0
        assertEquals(5400.0, line.lineTotal, 0.001)
    }
}
