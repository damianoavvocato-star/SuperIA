package it.superiaprivata.app.device

data class DeviceProfile(
    val manufacturer: String,
    val modelName: String,
    val androidVersion: String,
    val apiLevel: Int,
    val totalRamMb: Long,
    val availableRamMb: Long,
    val supportedAbis: List<String>,
    val freeStorageGb: Double,
    val thermalStatus: String
)
