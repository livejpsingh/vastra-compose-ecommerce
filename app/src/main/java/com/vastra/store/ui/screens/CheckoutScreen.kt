@file:OptIn(ExperimentalMaterial3Api::class)

package com.vastra.store.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vastra.store.data.model.Address
import com.vastra.store.ui.ShopUiState
import com.vastra.store.ui.components.EmptyState
import com.vastra.store.ui.components.SectionHeader
import com.vastra.store.ui.components.SummaryRow
import com.vastra.store.ui.components.VastraTopBar
import com.vastra.store.util.formatInrExact

private val paymentMethods = listOf(
    "UPI" to "UPI · Google Pay, PhonePe, Paytm",
    "Card" to "Credit or debit card",
    "Cash on delivery" to "Pay the courier when it arrives"
)

@Composable
fun CheckoutScreen(
    state: ShopUiState,
    onBack: () -> Unit,
    onPlaceOrder: (Address, String, onSuccess: (String) -> Unit, onError: (String) -> Unit) -> Unit,
    onOrderPlaced: () -> Unit,
    onEditBag: () -> Unit
) {
    val initialAddress = remember(state.savedAddress, state.profile) {
        if (state.savedAddress.isEmpty && (state.profile.name.isNotBlank() || state.profile.phone.isNotBlank())) {
            state.savedAddress.copy(
                fullName = state.profile.name,
                phone = state.profile.phone
            )
        } else {
            state.savedAddress
        }
    }
    var form by remember(initialAddress) { mutableStateOf(initialAddress) }
    var payment by remember { mutableStateOf(paymentMethods.first().first) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        VastraTopBar(title = "Checkout", onBack = onBack)

        if (state.cart.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Filled.ShoppingCart,
                    title = "Nothing to check out",
                    message = "Your bag is empty. Add a piece or two and come back.",
                    actionLabel = "Back to bag",
                    onAction = onEditBag
                )
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            SectionHeader("Delivery address")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                AddressFields(
                    address = form,
                    onChange = {
                        form = it
                        error = null
                    },
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(Modifier.height(18.dp))
            SectionHeader("Payment method")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(8.dp)) {
                    paymentMethods.forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (payment == value) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = payment == value,
                                onClick = { payment = value }
                            )
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            Text(
                text = "Demo app: simulated order placement without external gateway.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Spacer(Modifier.height(18.dp))
            SectionHeader("Order summary")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    state.cart.forEach { line ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${line.quantity} × ${line.product.title}" +
                                    (line.size?.let { " ($it)" } ?: ""),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = formatInrExact(line.lineTotal),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    SummaryRow("Subtotal", formatInrExact(state.subtotal))
                    SummaryRow(
                        "Delivery",
                        if (state.shipping == 0.0) "Free" else formatInrExact(state.shipping)
                    )
                    SummaryRow("Total payable", formatInrExact(state.total), emphasise = true)
                }
            }

            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 8.dp
        ) {
            Button(
                onClick = {
                    error = null
                    onPlaceOrder(
                        form,
                        payment,
                        { _ -> onOrderPlaced() },
                        { problem -> error = problem }
                    )
                },
                enabled = !state.isPlacingOrder,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (state.isPlacingOrder) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Place order · ${formatInrExact(state.total)}",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun AddressFields(
    address: Address,
    onChange: (Address) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = address.fullName,
            onValueChange = { onChange(address.copy(fullName = it)) },
            label = { Text("Full name") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = address.phone,
            onValueChange = { value ->
                onChange(address.copy(phone = value.filter { it.isDigit() }.take(10)))
            },
            label = { Text("Mobile number") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = address.line1,
            onValueChange = { onChange(address.copy(line1 = it)) },
            label = { Text("Flat / house no., building, street") },
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = address.city,
                onValueChange = { onChange(address.copy(city = it)) },
                label = { Text("City") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = address.state,
                onValueChange = { onChange(address.copy(state = it)) },
                label = { Text("State") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = address.pincode,
            onValueChange = { value ->
                onChange(address.copy(pincode = value.filter { it.isDigit() }.take(6)))
            },
            label = { Text("PIN code") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
