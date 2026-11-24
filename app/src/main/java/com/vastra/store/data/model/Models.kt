package com.vastra.store.data.model

/** A catalogue item. Prices are converted to INR when the product enters the app. */
data class Product(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val discountPercentage: Double = 0.0,
    val rating: Double = 0.0,
    val stock: Int = 0,
    val brand: String? = null,
    val thumbnail: String = "",
    val images: List<String> = emptyList()
) {
    /** Price actually charged, after the catalogue discount, in INR. */
    val finalPrice: Double
        get() = price * (1.0 - discountPercentage.coerceIn(0.0, 90.0) / 100.0)

    val gallery: List<String>
        get() = images.ifEmpty { listOf(thumbnail) }.filter { it.isNotBlank() }

    val inStock: Boolean get() = stock > 0

    val brandLabel: String get() = brand?.takeIf { it.isNotBlank() } ?: "Vastra Studio"
}

data class ProductsResponse(
    val products: List<Product> = emptyList(),
    val total: Int = 0,
    val skip: Int = 0,
    val limit: Int = 0
)

/** One row in the bag. [size] is null for products that are not sized. */
data class CartLine(
    val product: Product,
    val size: String? = null,
    val quantity: Int = 1
) {
    val lineTotal: Double get() = product.finalPrice * quantity
}

data class Address(
    val fullName: String = "",
    val phone: String = "",
    val line1: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = ""
) {
    val isEmpty: Boolean
        get() = fullName.isBlank() && phone.isBlank() && line1.isBlank() &&
            city.isBlank() && state.isBlank() && pincode.isBlank()

    val singleLine: String
        get() = listOf(line1, city, state, pincode).filter { it.isNotBlank() }.joinToString(", ")
}

data class Order(
    val id: String,
    val placedAt: Long,
    val lines: List<CartLine>,
    val subtotal: Double,
    val shipping: Double,
    val total: Double,
    val address: Address,
    val paymentMethod: String,
    val status: String = "Confirmed"
) {
    val itemCount: Int get() = lines.sumOf { it.quantity }
}
