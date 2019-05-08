package com.silencedut.fpsviewer.data

import android.util.SparseArray
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.silencedut.fpsviewer.utilities.FpsLog
import com.silencedut.hub_annotation.HubInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * @author SilenceDut
 * @date 2019/5/5
 */
@HubInject(api = [IJankRepository::class])
class JankRepository : IJankRepository {
    private  val monitor = Object()
    private val jankDao = FpsDatabase.getInstance().jankDao()
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

        val jankInfo =  JankInfo(
            System.currentTimeMillis(),
            frameCostMillis,
            frameIndex,
            stackCountEntries.map {
                Pair(it.key, it.value)
            })
        synchronized(monitor) {
            jankTraceInfosByBlockId.append(
                frameIndex,jankInfo)
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

}