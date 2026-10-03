package com.netinspector.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netinspector.app.core.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private val wifi = container.observeWifiSignal
    private val isp = container.getIspDetails
    private val firewall = container.scanFirewallStatus

    init {
        viewModelScope.launch { wifi().collect { value -> _uiState.update { it.copy(wifi = value, error = null) } } }
        refreshIsp()
    }

    fun refreshIsp() { viewModelScope.launch { _uiState.update { it.copy(loadingIsp = true, error = null) }; runCatching { isp() }.onSuccess { value -> _uiState.update { it.copy(isp = value, loadingIsp = false) } }.onFailure { error -> _uiState.update { it.copy(loadingIsp = false, error = error.message ?: "ISP lookup failed") } } } }
    fun scanFirewall() { viewModelScope.launch { _uiState.update { it.copy(scanning = true, error = null) }; runCatching { firewall() }.onSuccess { value -> _uiState.update { it.copy(firewall = value, scanning = false) } }.onFailure { error -> _uiState.update { it.copy(scanning = false, error = error.message ?: "Firewall scan failed") } } } }
}