package it.superiaprivata.app.device

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.os.StatFs
import java.util.Locale

class DeviceProfileReader(private val context: Context) {
    fun read(): DeviceProfile {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memory = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memory)

        val dataDirectory = StatFs(context.filesDir.absolutePath)
        val freeBytes = dataDirectory.availableBytes
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        val thermal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (powerManager.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> "Normale"
                PowerManager.THERMAL_STATUS_LIGHT -> "Lieve"
                PowerManager.THERMAL_STATUS_MODERATE -> "Moderato"
                PowerManager.THERMAL_STATUS_SEVERE -> "Elevato"
                PowerManager.THERMAL_STATUS_CRITICAL -> "Critico"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergenza"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "Spegnimento"
                else -> "Non disponibile"
            }
        } else "API non supportata"

        return DeviceProfile(
            manufacturer = Build.MANUFACTURER.orEmpty().replaceFirstChar { it.uppercase(Locale.getDefault()) },
            modelName = listOf(Build.BRAND, Build.MODEL).filterNotNull().distinct().joinToString(" "),
            androidVersion = Build.VERSION.RELEASE ?: "Sconosciuta",
            apiLevel = Build.VERSION.SDK_INT,
            totalRamMb = memory.totalMem / (1024L * 1024L),
            availableRamMb = memory.availMem / (1024L * 1024L),
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
            freeStorageGb = freeBytes / (1024.0 * 1024.0 * 1024.0),
            thermalStatus = thermal
        )
    }
}
