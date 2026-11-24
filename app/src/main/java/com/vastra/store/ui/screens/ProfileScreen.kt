@file:OptIn(ExperimentalMaterial3Api::class)

package com.vastra.store.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vastra.store.BuildConfig
import com.vastra.store.data.local.Profile
import com.vastra.store.data.model.Address
import com.vastra.store.ui.ShopUiState
import com.vastra.store.ui.components.SectionHeader
import com.vastra.store.ui.components.VastraTopBar
import com.vastra.store.util.Pricing
import com.vastra.store.util.formatInr

@Composable
fun ProfileScreen(
    state: ShopUiState,
    onSaveProfile: (Profile) -> Unit,
    onSaveAddress: (Address) -> Unit,
    onClearData: () -> Unit,
    onOpenOrders: () -> Unit
) {
    var editProfile by remember { mutableStateOf(false) }
    var editAddress by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        VastraTopBar(title = "Profile")

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(62.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = initialsOf(state.profile.name),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = state.profile.name.ifBlank { "Guest shopper" },
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (state.profile.email.isNotBlank()) {
                            Text(
                                text = state.profile.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (state.profile.phone.isNotBlank()) {
                            Text(
                                text = state.profile.phone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = { editProfile = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit profile")
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    value = state.orders.size.toString(),
                    label = "Orders",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenOrders
                )
                StatTile(
                    value = state.wishlist.size.toString(),
                    label = "Saved",
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = state.cartCount.toString(),
                    label = "In bag",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("Delivery address")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        if (state.savedAddress.isEmpty) {
                            Text(
                                text = "No address saved yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = state.savedAddress.fullName,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = state.savedAddress.singleLine,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = state.savedAddress.phone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    TextButton(onClick = { editAddress = true }) {
                        Text(if (state.savedAddress.isEmpty) "Add" else "Edit")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("About this app")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp)) {
                    AboutRow("App", "Vastra ${BuildConfig.VERSION_NAME}")
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    AboutRow("Catalogue", "DummyJSON demo API (dummyjson.com)")
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    AboutRow(
                        "Prices",
                        "Converted from USD at ${formatInr(Pricing.USD_TO_INR)} per dollar (demo rate)"
                    )
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    AboutRow(
                        "Delivery",
                        "Free over ${formatInr(Pricing.FREE_SHIPPING_INR)}, else ${formatInr(Pricing.SHIPPING_INR)}"
                    )
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    AboutRow("Payments", "Not connected — checkout is a simulation")
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    AboutRow("Storage", "Bag, wishlist and orders stay on this device")
                }
            }

            Spacer(Modifier.height(20.dp))
            OutlinedButton(
                onClick = { confirmClear = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Clear all app data", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (editProfile) {
        ProfileDialog(
            initial = state.profile,
            onDismiss = { editProfile = false },
            onSave = { profile ->
                onSaveProfile(profile)
                editProfile = false
            }
        )
    }

    if (editAddress) {
        AddressDialog(
            initial = state.savedAddress,
            onDismiss = { editAddress = false },
            onSave = { address ->
                onSaveAddress(address)
                editAddress = false
            }
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear app data?") },
            text = {
                Text(
                    "This removes the saved bag, wishlist, orders and profile from this device. " +
                        "It cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onClearData()
                    confirmClear = false
                }) {
                    Text("Clear data", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(88.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProfileDialog(
    initial: Profile,
    onDismiss: () -> Unit,
    onSave: (Profile) -> Unit
) {
    var draft by remember { mutableStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Your details") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { draft = draft.copy(name = it) },
                    label = { Text("Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = draft.email,
                    onValueChange = { draft = draft.copy(email = it.trim()) },
                    label = { Text("Email") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = draft.phone,
                    onValueChange = { value ->
                        draft = draft.copy(phone = value.filter { it.isDigit() }.take(10))
                    },
                    label = { Text("Mobile number") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(draft) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddressDialog(
    initial: Address,
    onDismiss: () -> Unit,
    onSave: (Address) -> Unit
) {
    var draft by remember { mutableStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delivery address") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                AddressFields(address = draft, onChange = { draft = it })
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(draft) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun initialsOf(name: String): String {
    val letters = name.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
    return letters.ifBlank { "V" }
}
