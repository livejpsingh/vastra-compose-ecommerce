package com.vastra.store.util

import com.vastra.store.data.model.Address
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Money rules and business utilities for the store.
 */
object Pricing {
    const val USD_TO_INR = 88.0
    const val FREE_SHIPPING_INR = 999.0
    const val SHIPPING_INR = 49.0
    const val MAX_QUANTITY_PER_LINE = 10
}

private val wholeRupees = DecimalFormat("#,##0")
private val paise = DecimalFormat("#,##0.00")

/** "₹1,299" — used everywhere prices are shown. */
fun formatInr(amount: Double): String = "₹" + wholeRupees.format(amount)

/** "₹1,299.50" — for line totals and the order summary. */
fun formatInrExact(amount: Double): String = "₹" + paise.format(amount)

fun shippingFor(subtotal: Double): Double =
    if (subtotal <= 0.0 || subtotal >= Pricing.FREE_SHIPPING_INR) 0.0 else Pricing.SHIPPING_INR

fun freeShippingShortfall(subtotal: Double): Double =
    (Pricing.FREE_SHIPPING_INR - subtotal).coerceAtLeast(0.0)

private val categoryLabels = mapOf(
    "mens-shirts" to "Men · Shirts",
    "mens-shoes" to "Men · Shoes",
    "mens-watches" to "Men · Watches",
    "womens-bags" to "Women · Bags",
    "womens-dresses" to "Women · Dresses",
    "womens-jewellery" to "Women · Jewellery",
    "womens-shoes" to "Women · Shoes",
    "womens-watches" to "Women · Watches",
    "tops" to "Tops"
)

fun categoryLabel(slug: String): String =
    categoryLabels[slug] ?: slug.replace('-', ' ').replaceFirstChar { it.uppercase() }

private val apparelSizes = listOf("XS", "S", "M", "L", "XL")
private val shoeSizes = listOf("UK 6", "UK 7", "UK 8", "UK 9", "UK 10")

fun sizesFor(category: String): List<String> = when (category) {
    "mens-shirts", "womens-dresses", "tops" -> apparelSizes
    "mens-shoes", "womens-shoes" -> shoeSizes
    else -> emptyList()
}

private val dateFormat = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale("en", "IN"))

fun formatOrderDate(timestamp: Long): String = dateFormat.format(Date(timestamp))

/** Returns a human readable problem, or null when the address can be used. */
fun validateAddress(address: Address): String? {
    if (address.fullName.trim().isBlank()) return "Enter the recipient's full name."
    val phone = address.phone.trim().filter { it.isDigit() }
    if (!phone.matches(Regex("^[6-9]\\d{9}$"))) {
        return "Enter a valid 10-digit Indian mobile number."
    }
    if (address.line1.trim().isBlank()) return "Enter the house / flat number and street."
    if (address.city.trim().isBlank()) return "Enter the city."
    if (address.state.trim().isBlank()) return "Enter the state."
    val pincode = address.pincode.trim().filter { it.isDigit() }
    if (!pincode.matches(Regex("^\\d{6}$"))) return "Enter a valid 6-digit PIN code."
    return null
}
