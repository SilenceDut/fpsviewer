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

    /**
     *  like TraceCompat.beginSection();
     *  对一些卡顿的堆栈进行标记，在UI上展示
     */
    fun appendSection(sectionName:String)

    /**
     *  like TraceCompat.endSection();
     */
    fun removeSection(sectionName:String)
}