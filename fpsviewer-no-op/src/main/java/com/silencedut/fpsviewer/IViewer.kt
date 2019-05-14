package com.silencedut.fpsviewer

import android.app.Application

interface IViewer  {
    fun initViewer(application: Application, fpsConfig: FpsConfig? = null)
    fun fpsConfig():FpsConfig
}