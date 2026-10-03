package com.netinspector.app.domain.usecase

import com.netinspector.app.domain.repository.*

class ObserveWifiSignalUseCase(private val repository: WifiRepository) { operator fun invoke() = repository.observe() }
class GetIspDetailsUseCase(private val repository: IspRepository) { suspend operator fun invoke() = repository.get() }
class ScanFirewallStatusUseCase(private val repository: FirewallRepository) { suspend operator fun invoke() = repository.scan() }