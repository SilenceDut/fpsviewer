package com.silencedut.fpsviewer.sniper


import android.content.Context
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import com.silencedut.diffadapter.DiffAdapter
import com.silencedut.fpsviewer.BaseFpsViewerActivity
import com.silencedut.fpsviewer.utilities.FpsLog
import com.silencedut.fpsviewer.R
import com.silencedut.fpsviewer.data.IJankRepository
import com.silencedut.fpsviewer.transfer.TransferCenter
import kotlinx.android.synthetic.main.fps_activity_jankdetails.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author SilenceDut
 * @date 2019/5/6
 */
private const val JANK_POINT = "JANK_POINT"
class JankDetailsActivity : BaseFpsViewerActivity() {

    companion object {
        fun navigation(context: Context,jankPoint:Int) {
            FpsLog.info("JankDetailsActivity navigation")
            val intent = Intent(context, JankDetailsActivity::class.java)
            intent.putExtra(JANK_POINT,jankPoint)
            context.startActivity(intent)
        }
    }

    private var mLastJankPoint = -1
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
            val jankPoint = it.getIntExtra(JANK_POINT,0)

            if(mLastJankPoint == jankPoint) {
                return
            }
            mLastJankPoint = jankPoint
            TransferCenter.getImpl(IJankRepository::class.java).jankDetailByPointData(it.getIntExtra(JANK_POINT,0))
                .observe(this, Observer { jankInfo ->

                    jankInfo?.occurredTime?.let { occurTime->
                        occurTimeTv.text = "发生时间:    ${ms2Date(occurTime)}"
                    }

                    costTimeTv.text = "耗时:    ${jankInfo?.frameCost?.toString()}ms"

                    mJankDetailsAdapter?.datas = jankInfo?.stackWitchCount?.map { pair->
                        JankDetailData(jankPoint, pair.second, pair.first)
                    }
                })
        }
    }


    private fun ms2Date(ms: Long): String {
        val date = Date(ms)
        val format = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
        return format.format(date)
    }

}