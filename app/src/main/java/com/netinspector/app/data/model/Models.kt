package com.netinspector.app.data.model

data class PortProbeResult(val port: Int, val status: String)

data class IspResponseDto(
    val ip: String,
    val org: String,
    val city: String,
    val region: String,
    val country: String,
    val asn: String
)