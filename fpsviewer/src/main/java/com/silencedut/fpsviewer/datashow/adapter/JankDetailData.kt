package com.silencedut.fpsviewer.datashow.adapter

import com.silencedut.diffadapter.data.BaseMutableData
import com.silencedut.fpsviewer.R

/**
 * @author SilenceDut
 * @date 2019/5/6
 */
data class JankDetailData(var jankId:Long, var count:Int, val stack:String) : BaseMutableData<JankDetailData>() {

    companion object {
        var VIEW_ID = R.layout.fps_holder_jank_detiles
    }

    override fun areUISame(data: JankDetailData): Boolean {
        return this.jankId == data.jankId
    }

    override fun uniqueItemFeature(): Any {
        return this.jankId
    }

    override fun getItemViewId(): Int {
        return VIEW_ID
    }

}