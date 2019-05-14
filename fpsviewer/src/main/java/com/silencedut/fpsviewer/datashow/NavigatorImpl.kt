package com.silencedut.fpsviewer.datashow

import android.content.Context
import android.content.Intent
import com.silencedut.fpsviewer.api.INavigator
import com.silencedut.fpsviewer.utilities.FpsLog
import com.silencedut.hub_annotation.HubInject

/**
 * @author SilenceDut
 * @date 2019-05-10
 */
@HubInject(api = [INavigator::class])
class NavigatorImpl : INavigator {

    override fun onCreate() {

    }

    override fun toFpsChatActivity(context: Context,fpsBuffer:IntArray,startFrameIndex : Int) {
        val intent = Intent(context, FpsChartActivity::class.java)

        intent.putExtra(FpsChartActivity.FPS_BUFFER, fpsBuffer)
        intent.putExtra(FpsChartActivity.FPS_BUFFER_START, startFrameIndex)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    override fun  toJankDetailsActivity(context: Context ,jankPoint:Int){
        FpsLog.info("JankDetailsActivity navigation")
        val intent = Intent(context, JankDetailsActivity::class.java)
        intent.putExtra(JANK_POINT,jankPoint)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    override fun toJankInfosActivity(context: Context, period: Boolean) {
        FpsLog.info("JankDetailsActivity navigation")
        val intent = Intent(context, JankStackInfosActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

}