package com.netinspector.app.core.network

import com.netinspector.app.data.model.PortProbeResult
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException

class SocketEngine {
    fun probe(host: String, port: Int, timeoutMs: Int = 900): PortProbeResult = try {
        Socket().use { socket ->
            socket.connect(InetSocketAddress(host, port), timeoutMs)
            PortProbeResult(port, "OPEN")
        }
    } catch (_: SocketTimeoutException) {
        PortProbeResult(port, "FILTERED")
    } catch (_: ConnectException) {
        PortProbeResult(port, "CLOSED")
    } catch (_: Exception) {
        PortProbeResult(port, "UNAVAILABLE")
    }
}