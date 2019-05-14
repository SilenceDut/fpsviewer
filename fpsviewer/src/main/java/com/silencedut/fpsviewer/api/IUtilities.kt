package com.silencedut.fpsviewer.api

import android.app.Application
import android.os.Handler
import com.silencedut.fpsviewer.transfer.ITransfer


/**
 * @author SilenceDut
 * @date 2019-05-14
 */
interface IUtilities:ITransfer {
    fun traceToString(skipStackCount: Int, stackArray: Array<Any>): String
    fun ms2Date(ms: Long): String
    fun mainHandler(): Handler
    fun application():Application
    fun setApplication(application: Application)
}