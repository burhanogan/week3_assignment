package com.example.week3_assignment.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.week3_assignment.data.model.DeviceCategory
import com.example.week3_assignment.data.model.SmartDevice
import com.example.week3_assignment.ui.theme.Week3AssignmentTheme

/**
 * "Route": ViewModel'i alan ve state'i toplayan tek yer.
 * Asıl ekran (HomeScreen) ViewModel'i hiç tanımaz → tamamen stateless.
 */
@Composable
fun HomeRoute(
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onRoomSelected = viewModel::onRoomSelected,
        onToggleDevice = viewModel::onToggleDevice
    )
}

/**
 * STATELESS ekran: sadece parametre alır, olayları lambda ile yukarı iletir.
 * İçinde .filter, toplama/çıkarma veya iş mantığı YOKTUR.
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRoomSelected: (String?) -> Unit,
    onToggleDevice: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Akıllı Ev Kontrol Merkezi",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                EnergyCard(
                    totalPowerText = uiState.totalPowerText,
                    activeDeviceCountText = uiState.activeDeviceCountText
                )
            }
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Cihaz ara") },
                    singleLine = true
                )
            }
            item {
                RoomFilterRow(
                    rooms = uiState.rooms,
                    selectedRoom = uiState.selectedRoom,
                    onRoomSelected = onRoomSelected
                )
            }
            if (uiState.isEmpty) {
                item {
                    Text(
                        text = "Aramanıza uygun cihaz bulunamadı.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }
            // key = id: liste değişince sadece değişen satır yeniden çizilir (performans).
            items(items = uiState.visibleDevices, key = { it.id }) { device ->
                DeviceItem(device = device, onToggle = { onToggleDevice(device.id) })
            }
        }
    }
}

@Composable
private fun EnergyCard(totalPowerText: String, activeDeviceCountText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "⚡ Anlık Enerji Tüketimi",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = totalPowerText,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = activeDeviceCountText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RoomFilterRow(
    rooms: List<String>,
    selectedRoom: String?,
    onRoomSelected: (String?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = selectedRoom == null,
                onClick = { onRoomSelected(null) },
                label = { Text("Tümü") }
            )
        }
        items(items = rooms, key = { it }) { room ->
            FilterChip(
                selected = selectedRoom == room,
                onClick = { onRoomSelected(room) },
                label = { Text(room) }
            )
        }
    }
}

@Composable
private fun DeviceItem(device: SmartDevice, onToggle: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = categoryIcon(device.category), style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${device.room} · ${device.powerWatt} W",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = statusText(device),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (device.isOnline) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
            if (device.isActive) {
                Button(onClick = onToggle, enabled = device.isOnline) { Text("Kapat") }
            } else {
                OutlinedButton(onClick = onToggle, enabled = device.isOnline) { Text("Aç") }
            }
        }
    }
}

// Sadece görsel eşleme (ikon/metin seçimi); iş mantığı değildir.
private fun categoryIcon(category: DeviceCategory): String = when (category) {
    DeviceCategory.LIGHT -> "💡"
    DeviceCategory.CAMERA -> "📷"
    DeviceCategory.THERMOSTAT -> "🌡️"
    DeviceCategory.VACUUM -> "🧹"
}

private fun statusText(device: SmartDevice): String = when {
    !device.isOnline -> "Çevrimdışı"
    device.isActive -> "Online · Açık"
    else -> "Online · Kapalı"
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    Week3AssignmentTheme {
        HomeScreen(
            uiState = HomeUiState(
                rooms = listOf("Salon", "Mutfak"),
                visibleDevices = listOf(
                    SmartDevice(1, "Köşe Lambası", DeviceCategory.LIGHT, "Salon", true, true, 15.0),
                    SmartDevice(9, "Balkon Aydınlatması", DeviceCategory.LIGHT, "Balkon", false, false, 25.0)
                ),
                totalPowerText = "15.0 W",
                activeDeviceCountText = "1 aktif cihaz"
            ),
            searchQuery = "",
            onSearchQueryChange = {},
            onRoomSelected = {},
            onToggleDevice = {}
        )
    }
}
