package com.silencedut.fpsviewer.jank

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.silencedut.fpsviewer.FpsConfig
import com.silencedut.fpsviewer.FpsViewer
import com.silencedut.fpsviewer.IViewer
import com.silencedut.fpsviewer.api.IUtilities
import com.silencedut.fpsviewer.data.FpsDatabase
import com.silencedut.fpsviewer.data.JankInfo
import com.silencedut.fpsviewer.transfer.TransferCenter
import com.silencedut.fpsviewer.utilities.FpsLog
import com.silencedut.hub_annotation.HubInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Collections

/**
 * @author SilenceDut
 * @date 2019/5/5
 */
@HubInject(api = [IJankRepository::class])
class JankRepository : IJankRepository {

    private val monitor = Object()
    private val jankDao = FpsDatabase.getInstance(TransferCenter.getImpl(IUtilities::class.java).application()).jankDao()
    private val jankTraceInfosByBlockId = SparseArray<JankInfo>()

    override fun onCreate() {
        GlobalScope.launch(Dispatchers.IO) {
            val cachedJanks = jankDao.getAllJankInfos()
            synchronized(monitor) {
                cachedJanks?.let {
                    it.forEach { jankInfo ->
                        FpsLog.info("cache $jankInfo \n")
                        jankTraceInfosByBlockId.append(jankInfo.jankPoint, jankInfo)
                    }
                }
            }
        }
    }


    override fun storeJankTraceInfo(
        frameIndex: Int,
        frameCostMillis: Int,
        stackCountEntries: List<MutableMap.MutableEntry<String, Int>>
    ) {

        val jankInfo = JankInfo(
            System.currentTimeMillis(),
            frameCostMillis,
            frameIndex,
            stackCountEntries.map {
                Pair(it.key, it.value)
            })
        synchronized(monitor) {
            jankTraceInfosByBlockId.append(
                frameIndex, jankInfo
            )
        }
        GlobalScope.launch(Dispatchers.IO) {
            jankDao.insert(jankInfo)
        }

    }

    override fun containsDetail(jankPoint: Int): Boolean {
        synchronized(monitor) {
            return jankTraceInfosByBlockId.get(jankPoint) != null
        }
    }

    override fun jankDetailByPointData(jankPoint: Int): LiveData<JankInfo?> {
        synchronized(monitor) {
            val jankInfoData = MutableLiveData<JankInfo?>()
            jankInfoData.value = jankTraceInfosByBlockId.get(jankPoint)
            return jankInfoData
        }
    }

    override fun jankInfosByTime(startTime: Long,sortByCostTime :Boolean): LiveData<List<JankInfo>> {

        return  MutableLiveData<List<JankInfo>>().also {

            TransferCenter.getImpl(IViewer::class.java).fpsConfig().taskExecutor.execute {
                it.postValue(mutableListOf<JankInfo>().also { jankInfoList ->
                    synchronized(monitor) {
                        var index = 0
                        while (index < jankTraceInfosByBlockId.size()) {
                            val jankInfoByIndex = jankTraceInfosByBlockId.valueAt(index)
                            if (jankInfoByIndex.occurredTime > startTime) {
                                jankInfoList.add(jankInfoByIndex)
                            }
                            index++
                        }
                    }
                    if (sortByCostTime) {
                        jankInfoList.sortByDescending { it.frameCost }
                    } else {
                        jankInfoList.sortByDescending { it.occurredTime }
                    }
                })
            }
        }
    }
}