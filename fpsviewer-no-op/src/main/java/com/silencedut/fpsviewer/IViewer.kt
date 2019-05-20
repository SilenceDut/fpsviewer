package com.silencedut.fpsviewer

import android.app.Application

interface IViewer  {
    fun initViewer(application: Application, fpsConfig: FpsConfig? = null)
    fun fpsConfig():FpsConfig
    fun appendSection(sectionName:String)
    fun removeSection(sectionName:String)
}