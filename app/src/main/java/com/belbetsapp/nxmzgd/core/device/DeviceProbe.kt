package com.belbetsapp.nxmzgd.core.device

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import java.util.Locale

class DeviceProbe(private val context: Context) {

    fun osVersionLabel(): String = "Android ${Build.VERSION.RELEASE}"

    fun language(): String = Locale.getDefault().language

    fun region(): String = Locale.getDefault().country

    fun deviceModel(): String = "${Build.MANUFACTURER} ${Build.MODEL}"

    fun batteryStatus(): String {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "NotCharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            else -> "Unknown"
        }
    }

    fun batteryLevel(): String {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level < 0 || scale <= 0) return "0"
        val pct = level.toFloat() / scale.toFloat()
        return if (pct >= 1f) "1" else String.format(Locale.US, "%.2f", pct)
    }

    fun isDeveloperOptionsEnabled(): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) == 1
        } catch (_: Exception) {
            false
        }
    }

    fun getNetworkCountry(): String {
        return try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            tm.networkCountryIso ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    fun getSimState(): String {
        return try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            when (tm.simState) {
                TelephonyManager.SIM_STATE_ABSENT -> "absent"
                TelephonyManager.SIM_STATE_READY -> "ready"
                TelephonyManager.SIM_STATE_PIN_REQUIRED -> "pin_required"
                TelephonyManager.SIM_STATE_PUK_REQUIRED -> "puk_required"
                TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "network_locked"
                TelephonyManager.SIM_STATE_NOT_READY -> "not_ready"
                TelephonyManager.SIM_STATE_PERM_DISABLED -> "perm_disabled"
                TelephonyManager.SIM_STATE_CARD_IO_ERROR -> "card_io_error"
                TelephonyManager.SIM_STATE_CARD_RESTRICTED -> "card_restricted"
                else -> "unknown"
            }
        } catch (_: Exception) {
            "error"
        }
    }
}
