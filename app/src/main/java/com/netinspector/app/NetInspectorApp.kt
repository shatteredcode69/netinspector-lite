package com.netinspector.app

import android.app.Application
import com.netinspector.app.core.di.AppContainer

class NetInspectorApp : Application() {
    val container by lazy { AppContainer(this) }
}