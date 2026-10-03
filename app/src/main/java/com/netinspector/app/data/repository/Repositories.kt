package com.netinspector.app.data.repository

import com.netinspector.app.core.network.SocketEngine
import com.netinspector.app.data.local.WifiLocalDataSource
import com.netinspector.app.data.remote.IspApiClient
import com.netinspector.app.domain.model.*
import com.netinspector.app.domain.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WifiRepositoryImpl(private val source: WifiLocalDataSource) : WifiRepository { override fun observe() = source.observe(); override fun gateway() = source.gateway() }
class IspRepositoryImpl(private val client: IspApiClient) : IspRepository {
    override suspend fun get() = withContext(Dispatchers.IO) { client.fetch().let { IspDetails(it.ip, it.org, listOf(it.city, it.region, it.country).filter(String::isNotBlank).joinToString(", "), it.asn) } }
}
class FirewallRepositoryImpl(private val engine: SocketEngine, private val wifi: WifiLocalDataSource) : FirewallRepository {
    override suspend fun scan() = withContext(Dispatchers.IO) {
        val gateway = wifi.gateway() ?: error("No gateway detected")
        val ports = listOf(53, 22, 80, 443, 8080, 8443).map { port -> engine.probe(gateway, port).let { PortStatus(it.port, it.status) } }
        FirewallAudit(gateway, ports)
    }
}