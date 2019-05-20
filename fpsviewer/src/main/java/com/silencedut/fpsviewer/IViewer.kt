package com.silencedut.fpsviewer

import android.app.Application
import com.silencedut.fpsviewer.transfer.ITransfer

/**
 * @author SilenceDut
 * @date 2019-05-14
 */
interface IViewer :ITransfer {
    fun initViewer(application: Application,fpsConfig: FpsConfig? = null)
    fun fpsConfig():FpsConfig
    fun appendSection(sectionName:String)
    fun removeSection(sectionName:String)
}