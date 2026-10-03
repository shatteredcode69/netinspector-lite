package com.netinspector.app

import android.os.Bundle
import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.netinspector.app.presentation.dashboard.DashboardScreen
import com.netinspector.app.presentation.dashboard.DashboardViewModel
import com.netinspector.app.presentation.theme.NetInspectorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as NetInspectorApp
        setContent {
            NetInspectorTheme {
                val permissions = remember {
                    buildList {
                        add(Manifest.permission.ACCESS_FINE_LOCATION)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.NEARBY_WIFI_DEVICES)
                    }.toTypedArray()
                }
                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
                LaunchedEffect(Unit) { launcher.launch(permissions) }
                DashboardScreen(viewModel<DashboardViewModel> { DashboardViewModel(app.container) })
            }
        }
    }
}