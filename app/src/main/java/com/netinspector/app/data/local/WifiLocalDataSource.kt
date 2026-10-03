package com.netinspector.app.data.local

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.netinspector.app.domain.model.WifiInfoModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class WifiLocalDataSource(private val context: Context) {
    private val connectivity = context.getSystemService(ConnectivityManager::class.java)
    private val wifi = context.applicationContext.getSystemService(WifiManager::class.java)

    fun observe(): Flow<WifiInfoModel?> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = trySend(read(network))
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) = trySend(read(network))
            override fun onLost(network: Network) = trySend(null)
        }
        connectivity.registerDefaultNetworkCallback(callback)
        trySend(read(connectivity.activeNetwork))
        awaitClose { connectivity.unregisterNetworkCallback(callback) }
    }

    fun gateway(network: Network? = connectivity.activeNetwork): String? =
        network?.let { connectivity.getLinkProperties(it) }?.routes?.firstOrNull { it.isDefaultRoute }?.gateway?.hostAddress

    private fun read(network: Network?): WifiInfoModel? {
        val caps = network?.let { connectivity.getNetworkCapabilities(it) } ?: return null
        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) caps.transportInfo as? WifiInfo else null ?: wifi.connectionInfo
        val canReadLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val ssid = if (canReadLocation) info.ssid.trim('"').ifBlank { "Unknown network" } else "Permission needed"
        return WifiInfoModel(ssid, if (canReadLocation) info.bssid ?: "Unknown" else "Hidden", info.rssi, info.linkSpeed, info.frequency, info.standardName())
    }

    @Suppress("DEPRECATION")
    private fun WifiInfo.standardName(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return "Wi-Fi"
        return when (wifiStandard) {
        WifiInfo.WIFI_STANDARD_11AX -> "Wi-Fi 6"
        WifiInfo.WIFI_STANDARD_11AC -> "Wi-Fi 5"
        WifiInfo.WIFI_STANDARD_11N -> "Wi-Fi 4"
        else -> "Wi-Fi"
        }
    }
}