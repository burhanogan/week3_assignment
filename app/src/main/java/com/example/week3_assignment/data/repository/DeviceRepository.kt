package com.example.week3_assignment.data.repository

import com.example.week3_assignment.data.model.SmartDevice
import kotlinx.coroutines.flow.StateFlow

/**
 * Veri katmanının sözleşmesi. ViewModel sadece bu arayüzü tanır;
 * verinin nereden geldiği (sahte liste, Room, ağ...) ViewModel'i ilgilendirmez.
 */
interface DeviceRepository {

    /** Tüm cihazların canlı listesi. Her değişiklikte yeni değer yayınlanır. */
    val devices: StateFlow<List<SmartDevice>>

    /** Cihazı açar/kapatır. suspend: ana thread'i bloklamadan arka planda çalışır. */
    suspend fun toggleDevice(deviceId: Int)
}
