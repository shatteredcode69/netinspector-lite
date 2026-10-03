package com.netinspector.app.presentation.dashboard

import com.netinspector.app.domain.model.*

data class DashboardUiState(val wifi: WifiInfoModel? = null, val isp: IspDetails? = null, val firewall: FirewallAudit? = null, val loadingIsp: Boolean = false, val scanning: Boolean = false, val error: String? = null)