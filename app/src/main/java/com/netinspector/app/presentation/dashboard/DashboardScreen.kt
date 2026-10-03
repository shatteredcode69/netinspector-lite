package com.netinspector.app.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.netinspector.app.domain.model.*

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    Scaffold { padding -> LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(padding)) {
        item { Text("NETINSPECTOR", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary); Text("Your connection, decoded.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item { SignalCard(state.wifi) }
        item { IspCard(state.isp, state.loadingIsp, viewModel::refreshIsp) }
        item { FirewallCard(state.firewall, state.scanning, viewModel::scanFirewall) }
        state.error?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }
    } }
}

@Composable private fun SignalCard(wifi: WifiInfoModel?) { SectionCard("Wi-Fi signal") { if (wifi == null) Text("No active Wi-Fi connection") else { Text(wifi.ssid, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Text("${wifi.rssi} dBm  •  ${wifi.linkSpeed} Mbps  •  ${wifi.standard}"); Text("${wifi.frequency} MHz  •  ${wifi.bssid}") } } }
@Composable private fun IspCard(isp: IspDetails?, loading: Boolean, refresh: () -> Unit) { SectionCard("Internet identity") { if (loading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else if (isp == null) { Text("Lookup unavailable"); Button(onClick = refresh) { Text("Retry") } } else { Text(isp.ip, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Text(isp.organization.ifBlank { "Organization unavailable" }); Text("${isp.location}  •  ${isp.asn.ifBlank { "ASN unavailable" }}") } } }
@Composable private fun FirewallCard(audit: FirewallAudit?, scanning: Boolean, scan: () -> Unit) { SectionCard("Gateway audit") { if (audit == null) Text("Probe common gateway ports for filtering and exposure.") else { Text("Gateway: ${audit.gateway}"); audit.ports.forEach { port -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Port ${port.port}"); Text(port.status, fontWeight = FontWeight.Bold, color = if (port.status == "OPEN") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) } } }; Button(onClick = scan, enabled = !scanning) { if (scanning) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Run audit") } } }
@Composable private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) { ElevatedCard { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = { Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary); content() }) } }