package com.example.week3_assignment.data.model

/** Cihaz kategorileri (ödevdeki ham veri setine göre). */
enum class DeviceCategory {
    LIGHT,
    CAMERA,
    THERMOSTAT,
    VACUUM
}

/**
 * Tek bir akıllı ev cihazı.
 * Immutable (değiştirilemez) bir data class'tır: durum değişince copy() ile yeni nesne üretilir,
 * böylece StateFlow değişikliği algılar ve arayüz otomatik güncellenir.
 */
data class SmartDevice(
    val id: Int,
    val name: String,
    val category: DeviceCategory,
    val room: String,
    val isOnline: Boolean,
    val isActive: Boolean,
    val powerWatt: Double
)
