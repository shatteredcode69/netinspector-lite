package com.netinspector.app.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(primary = Color(0xFF00796B), secondary = Color(0xFFE07A5F), background = Color(0xFFF4F7F5), surface = Color.White)

@Composable fun NetInspectorTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = LightColors, content = content) }