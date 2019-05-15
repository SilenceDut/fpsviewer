package com.silencedut.fpsviewer.jank

import androidx.lifecycle.LiveData
import com.silencedut.fpsviewer.data.JankInfo
import com.silencedut.fpsviewer.transfer.ITransfer


/**
 * @author SilenceDut
 * @date 2019/5/5
 */
interface IJankRepository : ITransfer {

    fun jankDetailByPointData(jankId: Long): LiveData<JankInfo?>

    fun storeJankTraceInfo(frameTimeMillis: Long,frameCostMillis: Int, stackCountEntries: List<MutableMap.MutableEntry<String, Int>>)

    fun jankInfosAfterTime(startTime: Long = 0, sortByCostTime :Boolean = true) :LiveData<List<JankInfo>>

    fun delete(jankId: Long)

    fun markResolved(jankId: Long)

    fun resolvedJankLiveData():LiveData<Long>
}