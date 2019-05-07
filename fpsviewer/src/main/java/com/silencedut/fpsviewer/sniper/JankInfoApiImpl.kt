package com.silencedut.fpsviewer.sniper

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.support.annotation.Nullable
import android.util.SparseArray
import com.silencedut.hub_annotation.HubInject


/**
 * @author SilenceDut
 * @date 2019/5/5
 */
@HubInject(api = [IJankInfoApi::class])
class JankInfoApiImpl : IJankInfoApi {

    private val jankTraceInfosByBlockId = SparseArray<JankInfo>()
    override fun onCreate() {
    }


    @Synchronized
    override fun storeJankTraceInfo(
        frameIndex: Int,
        frameCostMillis: Int,
        stackCountEntries: List<MutableMap.MutableEntry<String, Int>>) {

        jankTraceInfosByBlockId.append(frameIndex,
            JankInfo(System.currentTimeMillis(),frameCostMillis,frameIndex, stackCountEntries.map {
                Pair(it.key,it.value)
            })
        )
    }

    @Synchronized
    override fun containsDetail(jankPoint: Int): Boolean {
        return jankTraceInfosByBlockId.get(jankPoint)!=null
    }



    @Synchronized
    override fun jankDetailByPointData(jankPoint: Int): LiveData<JankInfo?> {
        val jankInfoData = MutableLiveData<JankInfo?>()
        jankInfoData.value = jankTraceInfosByBlockId.get(jankPoint)
        return jankInfoData
    }

}