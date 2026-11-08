package com.example.week3_assignment.data.repository

import com.example.week3_assignment.data.model.DeviceCategory
import com.example.week3_assignment.data.model.SmartDevice
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

/**
 * Ödevdeki ham veri setini bellekte tutan repository.
 * Gerçek bir uygulamada burası bir veritabanı ya da IoT sunucusu olurdu.
 */
class FakeDeviceRepository(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DeviceRepository {

    // private + Mutable: sadece repository değiştirebilir.
    private val _devices = MutableStateFlow(initialDevices())

    // public + read-only: dışarıya sadece okunabilir StateFlow verilir.
    override val devices: StateFlow<List<SmartDevice>> = _devices.asStateFlow()

    override suspend fun toggleDevice(deviceId: Int) {
        // I/O işi (ör. cihaza komut göndermek) ana thread'de değil, IO thread havuzunda yapılır.
        withContext(ioDispatcher) {
            // update{} atomiktir: aynı anda birden fazla tıklamada veri bozulmaz.
            _devices.update { list ->
                list.map { device ->
                    if (device.id == deviceId && device.isOnline) {
                        device.copy(isActive = !device.isActive)
                    } else {
                        device
                    }
                }
            }
        }
    }

    private fun initialDevices(): List<SmartDevice> = listOf(
        SmartDevice(1, "Köşe Lambası", DeviceCategory.LIGHT, "Salon", isOnline = true, isActive = true, powerWatt = 15.0),
        SmartDevice(2, "Tavan Avizesi", DeviceCategory.LIGHT, "Salon", isOnline = true, isActive = false, powerWatt = 60.0),
        SmartDevice(3, "Bahçe Kamerası", DeviceCategory.CAMERA, "Bahçe", isOnline = true, isActive = true, powerWatt = 8.5),
        SmartDevice(4, "Giriş Kamerası", DeviceCategory.CAMERA, "Antre", isOnline = true, isActive = true, powerWatt = 8.5),
        SmartDevice(5, "Merkezi Termostat", DeviceCategory.THERMOSTAT, "Salon", isOnline = true, isActive = true, powerWatt = 12.0),
        SmartDevice(6, "Yatak Odası Kliması", DeviceCategory.THERMOSTAT, "Yatak Odası", isOnline = true, isActive = false, powerWatt = 1200.0),
        SmartDevice(7, "Mutfak Spotları", DeviceCategory.LIGHT, "Mutfak", isOnline = true, isActive = false, powerWatt = 45.0),
        SmartDevice(8, "Roborock S7", DeviceCategory.VACUUM, "Koridor", isOnline = true, isActive = true, powerWatt = 50.0),
        SmartDevice(9, "Balkon Aydınlatması", DeviceCategory.LIGHT, "Balkon", isOnline = false, isActive = false, powerWatt = 25.0),
        SmartDevice(10, "Çalışma Odası Lambası", DeviceCategory.LIGHT, "Ofis", isOnline = true, isActive = true, powerWatt = 12.0)
    )
}
