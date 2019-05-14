package com.silencedut.fpsviewer.datashow

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.silencedut.diffadapter.DiffAdapter
import com.silencedut.fpsviewer.R
import com.silencedut.fpsviewer.api.IDisplayFps
import com.silencedut.fpsviewer.datashow.adapter.JankInfoData
import com.silencedut.fpsviewer.datashow.adapter.JankInfoHolder
import com.silencedut.fpsviewer.jank.IJankRepository
import com.silencedut.fpsviewer.transfer.TransferCenter
import kotlinx.android.synthetic.main.fps_jank_stack_infos.*

/**
 * @author SilenceDut
 * @date 2019-05-10
 */
class JankStackInfosActivity : BaseFpsViewerActivity() {
    private var mJanksAdapter: DiffAdapter?=null
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_period -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_all -> {
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }


    override fun provideContentViewId(): Int {
        return R.layout.fps_jank_stack_infos
    }

    override fun initViews() {
        jank_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        mJanksAdapter = DiffAdapter(this)
        mJanksAdapter?.registerHolder(
            JankInfoHolder::class.java,
            JankInfoData.VIEW_ID
        )
        janks_rv.adapter = mJanksAdapter
        janks_rv.layoutManager = LinearLayoutManager(this)
        TransferCenter.getImpl(IJankRepository::class.java).jankInfosByTime(0).observe(this, Observer {
            mJanksAdapter?.datas = it?.map { jankInfo->
                JankInfoData(jankInfo)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        TransferCenter.getImpl(IDisplayFps::class.java).show()
    }

}