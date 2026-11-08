package com.example.week3_assignment.ui.home

import com.example.week3_assignment.data.model.SmartDevice

/**
 * Ekranın ihtiyaç duyduğu HER ŞEY, ViewModel tarafından önceden hesaplanmış halde burada durur.
 * Compose UI bu nesneyi sadece okur ve çizer; hiçbir filtreleme/hesaplama yapmaz.
 */
data class HomeUiState(
    val rooms: List<String> = emptyList(),
    val selectedRoom: String? = null,          // null = "Tümü"
    val visibleDevices: List<SmartDevice> = emptyList(),
    val totalPowerText: String = "0.0 W",      // Aktif + online cihazların toplamı (hazır metin)
    val activeDeviceCountText: String = "0 aktif cihaz",
    val isEmpty: Boolean = false               // Filtre sonucu boşsa true
)
