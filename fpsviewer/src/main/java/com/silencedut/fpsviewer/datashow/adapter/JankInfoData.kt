package com.silencedut.fpsviewer.datashow.adapter

import com.silencedut.diffadapter.data.BaseMutableData
import com.silencedut.fpsviewer.R
import com.silencedut.fpsviewer.data.JankInfo

/**
 * @author SilenceDut
 * @date 2019/5/6
 */
data class JankInfoData(var jankInfo: JankInfo,var showDelete:Boolean = false) : BaseMutableData<JankInfoData>() {

    companion object {
        var VIEW_ID = R.layout.fps_holder_jank_info
    }

    override fun areUISame(data: JankInfoData): Boolean {
        return this.jankInfo.occurredTime == data.jankInfo.occurredTime
    }

    override fun uniqueItemFeature(): Any {
        return this.jankInfo.occurredTime
    }

    override fun getItemViewId(): Int {
        return VIEW_ID
    }

}