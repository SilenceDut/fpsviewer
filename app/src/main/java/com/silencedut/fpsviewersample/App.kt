package com.silencedut.fpsviewersample

import android.app.Application
import com.silencedut.fpsviewer.FpsViewer
import com.squareup.leakcanary.LeakCanary

/**
 * @author SilenceDut
 * @date 2019-05-14
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FpsViewer.getViewer().initViewer(this)
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
    }
}