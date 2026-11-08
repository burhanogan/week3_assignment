package com.example.week3_assignment.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.week3_assignment.data.model.SmartDevice
import com.example.week3_assignment.data.repository.DeviceRepository
import com.example.week3_assignment.data.repository.FakeDeviceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * MVVM'in "VM" katmanı. Tüm filtreleme ve toplama işlemleri burada yapılır.
 *
 * Akış:
 *   repository.devices ─┐
 *   searchQuery ────────┼─ combine ─▶ (Dispatchers.Default'ta hesapla) ─▶ uiState (StateFlow) ─▶ Compose UI
 *   selectedRoom ───────┘
 */
class HomeViewModel(
    private val repository: DeviceRepository,
    computationDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    // Kullanıcı girdileri (private Mutable, dışarıya sadece fonksiyonlarla değiştirilir)
    private val _searchQuery = MutableStateFlow("")
    private val selectedRoom = MutableStateFlow<String?>(null)

    /**
     * Arama kutusunun metni ayrı ve anlık (senkron) yayınlanır.
     * Böylece arka plan hesaplaması sürerken bile yazılan harfler kaybolmaz, imleç zıplamaz.
     */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** UI'ın gözlemlediği tek durum kaynağı (Single Source of Truth). */
    val uiState: StateFlow<HomeUiState> =
        combine(repository.devices, _searchQuery, selectedRoom) { devices, query, room ->
            buildUiState(devices, query, room)
        }
            // Hesaplamalar ana (UI) thread'de değil, CPU işleri için ayrılmış Default havuzunda çalışır.
            // Böylece arama yazarken veya butona basarken ekran donmaz.
            .flowOn(computationDispatcher)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState()
            )

    // ---- UI'dan gelen olaylar (event) ----

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onRoomSelected(room: String?) {
        selectedRoom.value = room
    }

    fun onToggleDevice(deviceId: Int) {
        // Coroutine viewModelScope içinde başlar; ekran kapanırsa otomatik iptal edilir.
        viewModelScope.launch {
            repository.toggleDevice(deviceId)
        }
    }

    // ---- Saf hesaplama fonksiyonu (UI'da OLMAYAN filtre ve toplama mantığı) ----

    private fun buildUiState(
        devices: List<SmartDevice>,
        query: String,
        room: String?
    ): HomeUiState {
        val trimmedQuery = query.trim()

        // 1) Arama + oda filtresi
        val visible = devices.filter { device ->
            val matchesQuery = trimmedQuery.isEmpty() ||
                device.name.contains(trimmedQuery, ignoreCase = true)
            val matchesRoom = room == null || device.room == room
            matchesQuery && matchesRoom
        }

        // 2) Reaktif enerji sayacı: SADECE online VE aktif cihazlar toplanır (tüm evin anlık tüketimi)
        val poweredDevices = devices.filter { it.isOnline && it.isActive }
        val totalPower = poweredDevices.sumOf { it.powerWatt }

        return HomeUiState(
            rooms = devices.map { it.room }.distinct(),
            selectedRoom = room,
            visibleDevices = visible,
            totalPowerText = String.format(Locale.US, "%.1f W", totalPower),
            activeDeviceCountText = "${poweredDevices.size} aktif cihaz",
            isEmpty = visible.isEmpty()
        )
    }

    companion object {
        /** Basit manuel bağımlılık enjeksiyonu (Hilt kullanmadan). */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(repository = FakeDeviceRepository())
            }
        }
    }
}
