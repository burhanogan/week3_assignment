# Akıllı Ev (IoT) Kontrol Merkezi — week3_assignment

Kotlin + Jetpack Compose ile yazılmış; MVVM, Jetpack ViewModel, Coroutines ve StateFlow kullanan akıllı ev kontrol uygulaması.

## Özellikler
- **Arama ve oda filtresi:** Cihaz adına göre arama ve oda çipleri (Tümü, Salon, Mutfak...) aynı anda çalışır.
- **Reaktif enerji sayacı:** En üstteki kart, sadece **online ve aktif** cihazların toplam tüketimini gösterir.
- **Aç/Kapat:** Butona basınca cihazın durumu değişir, sayaç anında güncellenir. Çevrimdışı cihazların butonu devre dışıdır.

## Mimari
```
data/
  model/SmartDevice.kt            → Immutable veri modeli + DeviceCategory
  repository/DeviceRepository.kt  → Veri katmanı arayüzü
  repository/FakeDeviceRepository.kt → MutableStateFlow + Dispatchers.IO
ui/
  home/HomeUiState.kt             → Ekranın hazır hesaplanmış durumu
  home/HomeViewModel.kt           → combine + flowOn(Default) + stateIn
  home/HomeScreen.kt              → Stateless Compose UI
```

- **Kural 1 (Katman ayrımı):** Tüm `.filter` ve toplama (`sumOf`) işlemleri `HomeViewModel` içinde yapılır. `HomeScreen` yalnızca hazır `HomeUiState` değerini çizer ve olayları lambda ile ViewModel'e iletir.
- **Kural 2 (Performans):** Filtre ve toplam hesabı `flowOn(Dispatchers.Default)` ile ana thread dışında yapılır, cihaz komutları `Dispatchers.IO` üzerinde çalışır. Listede `key = id` kullanıldığı için yalnızca değişen satır yeniden çizilir. Arama metni ayrı bir StateFlow ile anlık yansıtıldığından yazarken takılma olmaz.
