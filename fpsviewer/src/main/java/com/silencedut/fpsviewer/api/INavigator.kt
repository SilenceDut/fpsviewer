package com.silencedut.fpsviewer.api

import android.content.Context
import com.silencedut.fpsviewer.transfer.ITransfer

/**
 * @author SilenceDut
 * @date 2019-05-10
 */
interface INavigator : ITransfer{
    fun toFpsChatActivity(context: Context,fpsBuffer:IntArray,startFrameIndex : Int)
    fun toJankDetailsActivity(context: Context ,jankPoint:Int)
    fun toJankInfosActivity(context:Context,period: Boolean = true)
}