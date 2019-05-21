package com.silencedut.fpsviewer.jank

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData

import com.silencedut.fpsviewer.api.IUtilities
import com.silencedut.fpsviewer.data.FpsDatabase
import com.silencedut.fpsviewer.data.JankInfo
import com.silencedut.fpsviewer.transfer.TransferCenter
import com.silencedut.fpsviewer.utilities.FpsLog
import com.silencedut.hub_annotation.HubInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * @author SilenceDut
 * @date 2019/5/5
 */
@HubInject(api = [IJankRepository::class])
class JankRepository : IJankRepository {

    private val jankDao = FpsDatabase.getInstance(TransferCenter.getImpl(IUtilities::class.java).application()).jankDao()
    private val jankTraceInfosByJankId = ConcurrentHashMap<Long,JankInfo>()
    private val resolvedJankLiveData = MutableLiveData<Long>()

    override fun onCreate() {
        GlobalScope.launch(Dispatchers.IO) {
            val cachedJanks = jankDao.getAllJankInfos()
            cachedJanks?.let {
                it.forEach { jankInfo ->
                    FpsLog.info("cache $jankInfo \n")
                    jankTraceInfosByJankId[jankInfo.occurredTime] = jankInfo
                }
            }
        }
    }

    override fun resolvedJankLiveData(): LiveData<Long> {
        return resolvedJankLiveData
    }


    override fun delete(jankId: Long) {
        jankTraceInfosByJankId.remove(jankId)
        GlobalScope.launch(Dispatchers.IO){
            jankDao.delete(jankId)
        }
    }

    override fun markResolved(jankId: Long) {
        GlobalScope.launch(Dispatchers.IO){
            jankTraceInfosByJankId[jankId]?.let {
                it.resolved = true
                jankDao.update(it)
                resolvedJankLiveData.postValue(jankId)
            }
        }
    }



    override fun storeJankTraceInfo(
        frameTimeMillis:Long,
        frameCostMillis: Int,
        stackCountEntries: List<MutableMap.MutableEntry<String, Int>>,
        section:List<String>
    ) {

        val jankInfo = JankInfo(
            frameTimeMillis,
            frameCostMillis,
            stackCountEntries.map {
                Pair(it.key, it.value)
            },
            section)
        jankTraceInfosByJankId[frameTimeMillis] = jankInfo
        GlobalScope.launch(Dispatchers.IO) {
            jankDao.insert(jankInfo)
        }

    }


    override fun jankDetailByPointData(jankId: Long): LiveData<JankInfo?> {
        val jankInfoData = MutableLiveData<JankInfo?>()
        jankInfoData.value = jankTraceInfosByJankId[jankId]
        return jankInfoData
    }

    override fun jankInfosAfterTime(startTime: Long, sortByCostTime :Boolean): LiveData<List<JankInfo>> {

        return  MutableLiveData<List<JankInfo>>().also {
            GlobalScope.launch(Dispatchers.Default){
                it.postValue(mutableListOf<JankInfo>().also { jankInfoList ->
                    jankTraceInfosByJankId.forEach{
                        if(it.key > startTime) {
                            jankInfoList.add(it.value)
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