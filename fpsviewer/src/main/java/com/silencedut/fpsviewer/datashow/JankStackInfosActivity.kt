package com.silencedut.fpsviewer.datashow

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.SharedPreferences
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.view.View


import com.silencedut.diffadapter.DiffAdapter
import com.silencedut.diffadapter.rvhelper.RvHelper
import com.silencedut.diffadapter.utils.UpdateFunction
import com.silencedut.fpsviewer.R
import com.silencedut.fpsviewer.api.IDisplayFps
import com.silencedut.fpsviewer.datashow.adapter.JankInfoData
import com.silencedut.fpsviewer.datashow.adapter.JankInfoHolder
import com.silencedut.fpsviewer.jank.IJankRepository
import com.silencedut.fpsviewer.transfer.TransferCenter
import com.silencedut.fpsviewer.utilities.FpsConstants
import kotlinx.android.synthetic.main.fps_jank_activity_stackinfos.*
import kotlinx.android.synthetic.main.fps_sanckbar_sort.*
import java.util.concurrent.locks.ReentrantLock

/**
 * @author SilenceDut
 * @date 2019-05-10
 */
class JankStackInfosActivity : BaseFpsViewerActivity() {
    private var mJanksAdapter: DiffAdapter?=null
    private var currentByCostTime = true
    private var startTime  = TransferCenter.getImpl(IDisplayFps::class.java).periodStartTime()
    private var sharedPreferences : SharedPreferences? = null
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        when (item.itemId) {
            R.id.navigation_period -> {
                startTime = TransferCenter.getImpl(IDisplayFps::class.java).periodStartTime()
            }
            R.id.navigation_all -> {
                startTime = 0
            }
        }
        showJankInfos(startTime,currentByCostTime)
        true
    }

    private fun showSortToast() {
        val text = if(currentByCostTime) getString(R.string.sort_by_cost_time) else getString(R.string
            .sort_by_occurrence_time)
        if(sharedPreferences?.getBoolean(text,true) == true) {
            Snackbar.make(fps_jankInfos_rootView, text, Snackbar.LENGTH_LONG).setAction(R.string.not_prompted) {
                sharedPreferences?.edit()?.putBoolean(text,false)?.apply()
            }.show()
        }

    }

    override fun provideContentViewId(): Int {
        return R.layout.fps_jank_activity_stackinfos
    }

    override fun initViews() {
        sharedPreferences = getSharedPreferences(FpsConstants.ShAREDPREFERENCES, Context.MODE_PRIVATE)
        jank_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        jank_navigation.itemIconTintList = null
        mJanksAdapter = DiffAdapter(this)
        mJanksAdapter?.registerHolder(
            JankInfoHolder::class.java,
            JankInfoData.VIEW_ID
        )
        janks_rv.adapter = mJanksAdapter
        janks_rv.layoutManager = LinearLayoutManager(this)

        showJankInfos(startTime,currentByCostTime)
        showSortToast()

        sort?.setOnClickListener {
            currentByCostTime = !currentByCostTime
            showJankInfos(startTime,currentByCostTime)
            showSortToast()
        }

        mJanksAdapter?.addUpdateMediator(TransferCenter.getImpl(IJankRepository::class.java).resolvedJankLiveData(),
            object :UpdateFunction<Long, JankInfoData> {
                override fun providerMatchFeature(input: Long): Long {
                   return input
                }

                override fun applyChange(input: Long, originalData: JankInfoData): JankInfoData {
                    originalData.jankInfo.resolved = true
                    return originalData
                }
            })
    }

    private fun showJankInfos(startTime: Long = 0, sortByCostTime :Boolean = true){
        TransferCenter.getImpl(IJankRepository::class.java).jankInfosAfterTime(startTime,sortByCostTime).observe(this, Observer {
            mJanksAdapter?.datas = it?.map { jankInfo->
                JankInfoData(jankInfo)
            }
            janks_rv?.postDelayed({
                janks_rv.scrollToPosition(0)
            },200)
        })
    }
}