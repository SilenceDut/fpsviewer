package com.silencedut.fpsviewer.sniper

import android.arch.lifecycle.LiveData
import com.silencedut.fpsviewer.transfer.ITransfer


/**
 * @author SilenceDut
 * @date 2019/5/5
 */
interface IJankInfoApi : ITransfer {

    fun containsDetail(jankPoint: Int):Boolean

    fun jankDetailByPointData(jankPoint: Int):LiveData<JankInfo?>

    fun storeJankTraceInfo(frameIndex: Int, frameCostMillis: Int, stackCountEntries: List<MutableMap.MutableEntry<String, Int>>)

}