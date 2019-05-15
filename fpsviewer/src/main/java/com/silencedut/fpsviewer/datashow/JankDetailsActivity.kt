package com.silencedut.fpsviewer.datashow


import android.content.Intent
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import com.silencedut.diffadapter.DiffAdapter
import com.silencedut.fpsviewer.utilities.FpsLog
import com.silencedut.fpsviewer.R
import com.silencedut.fpsviewer.api.IUtilities
import com.silencedut.fpsviewer.jank.IJankRepository
import com.silencedut.fpsviewer.datashow.adapter.JankDetailData
import com.silencedut.fpsviewer.datashow.adapter.JankDetailHolder
import com.silencedut.fpsviewer.transfer.TransferCenter
import kotlinx.android.synthetic.main.fps_activity_jankdetails.*


/**
 * @author SilenceDut
 * @date 2019/5/6
 */
const val JANK_ID = "JANK_ID"
class JankDetailsActivity : BaseFpsViewerActivity() {

    private var mLastJankId = -1L
    private var mJankDetailsAdapter:DiffAdapter?=null

    override fun provideContentViewId(): Int {
        return R.layout.fps_activity_jankdetails
    }

    override fun initViews() {
        mJankDetailsAdapter = DiffAdapter(this)
        mJankDetailsAdapter?.registerHolder(
            JankDetailHolder::class.java,
            JankDetailData.VIEW_ID
        )
        jankDetailsRv.adapter = mJankDetailsAdapter
        jankDetailsRv.layoutManager = LinearLayoutManager(this)
        processData(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        processData(intent)
        FpsLog.info("JankDetailsActivity onNewIntent")
    }

    private fun processData(intent: Intent?) {
        intent?.let {
            val jankId = it.getLongExtra(JANK_ID,0)

            if(mLastJankId == jankId) {
                return
            }
            mLastJankId = jankId
            TransferCenter.getImpl(IJankRepository::class.java).jankDetailByPointData(it.getLongExtra(JANK_ID,0))
                .observe(this, Observer { jankInfo ->

                        if(jankInfo?.resolved == true){
                            resolve_status.setImageResource(R.mipmap.fps_done)
                        }else {
                            resolve_status.setOnClickListener {
                                TransferCenter.getImpl(IJankRepository::class.java).markResolved(jankId)
                                resolve_status.setImageResource(R.mipmap.fps_done)
                            }
                        }

                        jankInfo?.occurredTime?.let { occurTime->
                            occurTimeTv.text = getString(R.string.occurrence_time,TransferCenter.getImpl(IUtilities::class.java).ms2Date(occurTime))
                        }

                        costTimeTv.text = getString(R.string.cost_time,jankInfo?.frameCost.toString())

                        mJankDetailsAdapter?.datas = jankInfo?.stackWitchCount?.map { pair->
                        JankDetailData(jankId, pair.second, pair.first)
                    }
                })
        }
    }
}