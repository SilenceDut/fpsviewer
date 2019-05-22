package com.silencedut.fpsviewersample

import android.app.Application
import com.silencedut.fpsviewer.FpsViewer


/**
 * @author SilenceDut
 * @date 2019-05-14
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FpsViewer.getViewer().initViewer(this,null);
    }
}