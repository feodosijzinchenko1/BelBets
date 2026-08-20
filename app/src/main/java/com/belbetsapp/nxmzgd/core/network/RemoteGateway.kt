package com.belbetsapp.nxmzgd.core.network

import com.belbetsapp.nxmzgd.core.device.DeviceProbe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class RemoteGateway(private val device: DeviceProbe) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .cache(null)
        .build()

    private fun execute(address: String): String? {
        val builder = Request.Builder()
            .url(address)
            .cacheControl(CacheControl.Builder().noCache().noStore().build())
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .header("Pragma", "no-cache")
            .header("Expires", "0")
            .get()
            .build()
        repeat(3) {
            try {
                val resp = client.newCall(builder).execute()
                val body = resp.body?.string()
                resp.close()
                if (resp.isSuccessful && !body.isNullOrBlank()) return body
            } catch (_: Throwable) {
            }
        }
        return null
    }

    suspend fun fetchHandshake(): String? = withContext(Dispatchers.IO) {
        val query = buildString {
            append("https://sghfdva.top/api-belbets/info/?p=hfDHfdHfdHFDH")
            append("&os=").append(device.osVersionLabel())
            append("&lng=").append(device.language())
            append("&loc=").append(device.region())
            append("&devicemodel=").append(device.deviceModel())
            append("&bs=").append(device.batteryStatus())
            append("&bl=").append(device.batteryLevel())
            append("&nc=").append(device.getNetworkCountry())
            append("&sm=").append(device.getSimState())
            append("&dv=").append(if (device.isDeveloperOptionsEnabled()) "1" else "0")
        }
        execute(query)
    }
}
