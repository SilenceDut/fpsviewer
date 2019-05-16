package com.silencedut.fpsviewer.api

import android.content.Context
import com.silencedut.fpsviewer.transfer.ITransfer

/**
 * @author SilenceDut
 * @date 2019-05-10
 */
interface INavigator : ITransfer{
    fun toFpsChatActivity(context: Context)
    fun toJankDetailsActivity(context: Context, jankId:Long)
    fun toJankInfosActivity(context:Context)
}