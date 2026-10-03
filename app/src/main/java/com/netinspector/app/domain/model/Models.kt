package com.netinspector.app.domain.model

data class WifiInfoModel(val ssid: String, val bssid: String, val rssi: Int, val linkSpeed: Int, val frequency: Int, val standard: String)
data class IspDetails(val ip: String, val organization: String, val location: String, val asn: String)
data class FirewallAudit(val gateway: String, val ports: List<PortStatus>, val completed: Boolean = true)
data class PortStatus(val port: Int, val status: String)