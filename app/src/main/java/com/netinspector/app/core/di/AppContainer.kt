package com.netinspector.app.core.di

import android.content.Context
import com.netinspector.app.core.network.SocketEngine
import com.netinspector.app.data.local.WifiLocalDataSource
import com.netinspector.app.data.remote.IspApiClient
import com.netinspector.app.data.repository.FirewallRepositoryImpl
import com.netinspector.app.data.repository.IspRepositoryImpl
import com.netinspector.app.data.repository.WifiRepositoryImpl
import com.netinspector.app.domain.usecase.GetIspDetailsUseCase
import com.netinspector.app.domain.usecase.ObserveWifiSignalUseCase
import com.netinspector.app.domain.usecase.ScanFirewallStatusUseCase

class AppContainer(context: Context) {
    private val wifiSource = WifiLocalDataSource(context)
    private val socketEngine = SocketEngine()
    private val wifiRepository = WifiRepositoryImpl(wifiSource)
    private val ispRepository = IspRepositoryImpl(IspApiClient())
    private val firewallRepository = FirewallRepositoryImpl(socketEngine, wifiSource)

    val observeWifiSignal = ObserveWifiSignalUseCase(wifiRepository)
    val getIspDetails = GetIspDetailsUseCase(ispRepository)
    val scanFirewallStatus = ScanFirewallStatusUseCase(firewallRepository)
}