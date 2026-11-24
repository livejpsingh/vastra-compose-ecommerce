package com.vastra.store

import com.vastra.store.data.model.Address
import com.vastra.store.util.Pricing
import com.vastra.store.util.categoryLabel
import com.vastra.store.util.formatInr
import com.vastra.store.util.formatInrExact
import com.vastra.store.util.freeShippingShortfall
import com.vastra.store.util.shippingFor
import com.vastra.store.util.sizesFor
import com.vastra.store.util.validateAddress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PricingTest {

    @Test
    fun testFormatInr() {
        assertEquals("₹1,299", formatInr(1299.0))
        assertEquals("₹0", formatInr(0.0))
        assertEquals("₹10,500", formatInr(10500.0))
    }

    @Test
    fun testFormatInrExact() {
        assertEquals("₹1,299.50", formatInrExact(1299.5))
        assertEquals("₹49.00", formatInrExact(49.0))
    }

    @Test
    fun testShippingRules() {
        assertEquals(Pricing.SHIPPING_INR, shippingFor(500.0), 0.001)
        assertEquals(0.0, shippingFor(Pricing.FREE_SHIPPING_INR), 0.001)
        assertEquals(0.0, shippingFor(1500.0), 0.001)
        assertEquals(0.0, shippingFor(0.0), 0.001)
    }

    @Test
    fun testFreeShippingShortfall() {
        assertEquals(299.0, freeShippingShortfall(700.0), 0.001)
        assertEquals(0.0, freeShippingShortfall(1000.0), 0.001)
    }

    @Test
    fun testCategoryLabelMapping() {
        assertEquals("Men · Shirts", categoryLabel("mens-shirts"))
        assertEquals("Women · Bags", categoryLabel("womens-bags"))
        assertEquals("Casual wear", categoryLabel("casual-wear"))
    }

    @Test
    fun testSizesForCategory() {
        val apparel = sizesFor("mens-shirts")
        assertTrue(apparel.contains("M"))
        assertTrue(apparel.contains("XL"))

        val shoes = sizesFor("mens-shoes")
        assertTrue(shoes.contains("UK 8"))

        val accessories = sizesFor("womens-bags")
        assertTrue(accessories.isEmpty())
    }

    @Test
    fun testValidateAddress() {
        val valid = Address(
            fullName = "Ananya Sharma",
            phone = "9876543210",
            line1 = "42, MG Road",
            city = "Bengaluru",
            state = "Karnataka",
            pincode = "560001"
        )
        assertNull(validateAddress(valid))

        // Trimming handles accidental spaces
        val validWithSpaces = valid.copy(phone = " 9876543210 ", pincode = " 560001 ")
        assertNull(validateAddress(validWithSpaces))

        // Missing name
        assertNotNull(validateAddress(valid.copy(fullName = "")))

        // Invalid Indian mobile number (must be 10 digits starting 6-9)
        assertNotNull(validateAddress(valid.copy(phone = "1234567890")))
        assertNotNull(validateAddress(valid.copy(phone = "98765")))

        // Invalid PIN code (must be 6 digits)
        assertNotNull(validateAddress(valid.copy(pincode = "5600")))
        assertNotNull(validateAddress(valid.copy(pincode = "ABCDEF")))
    }
}
