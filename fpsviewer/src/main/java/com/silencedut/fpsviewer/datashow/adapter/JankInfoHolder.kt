package com.silencedut.fpsviewer.datashow.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.silencedut.diffadapter.DiffAdapter
import com.silencedut.diffadapter.holder.BaseDiffViewHolder
import com.silencedut.fpsviewer.api.INavigator
import com.silencedut.fpsviewer.R
import com.silencedut.fpsviewer.api.IUtilities
import com.silencedut.fpsviewer.jank.IJankRepository
import com.silencedut.fpsviewer.transfer.TransferCenter
import android.R.attr.tag
import com.silencedut.fpsviewer.datashow.TagLayout
import com.silencedut.fpsviewer.utilities.FpsLog

/**
 * @author SilenceDut
 * @date 2019/5/6
 */
class JankInfoHolder(itemView: View, diffAdapter: DiffAdapter) :
    BaseDiffViewHolder<JankInfoData>(itemView, diffAdapter) {
    private var occurredTimeTv: TextView = itemView.findViewById(R.id.occurTime_Tv)
    private var costTimeTv: TextView = itemView.findViewById(R.id.costTime_Tv)
    private var breviaryTv: TextView = itemView.findViewById(R.id.breviary_Tv)
    private var solveStatus: ImageView = itemView.findViewById(R.id.solve_status)
    private var delete: ImageView = itemView.findViewById(R.id.jank_delete)
    private var sectionTagLayout: TagLayout = itemView.findViewById(R.id.jank_section_tags)

    init {
        itemView.setOnClickListener {
            data?.let {
                TransferCenter.getImpl(INavigator::class.java).toJankDetailsActivity(context, it.jankInfo.occurredTime)
            }
            delete.visibility = View.GONE
        }

        delete.setOnClickListener {
            data?.let {
                TransferCenter.getImpl(IJankRepository::class.java).delete(it.jankInfo.occurredTime)
                diffAdapter.deleteData(data)
            }
        }

        itemView.setOnLongClickListener {
            data?.showDelete = true
            delete.visibility = View.VISIBLE
            return@setOnLongClickListener true
        }
    }

    override fun updateItem(data: JankInfoData, position: Int) {

        occurredTimeTv.text = context.getString(R.string.occurrence_time,
            TransferCenter.getImpl(IUtilities::class.java).ms2Date(data.jankInfo.occurredTime))
        costTimeTv.text = context.getString(R.string.cost_time, data.jankInfo.frameCost.toString())

        if (data.jankInfo.stackWitchCount.isNotEmpty()) {
            breviaryTv.text = data.jankInfo.stackWitchCount[0].first
        }
        if (data.jankInfo.resolved) {
            solveStatus.setImageResource(R.mipmap.fps_done)
        } else {
            solveStatus.setImageResource(R.mipmap.fps_alarm)
        }
        delete.visibility = if (data.showDelete) View.VISIBLE else View.GONE

        sectionTagLayout.removeAllViews()
        data.jankInfo.section.forEach {
            sectionTagLayout.visibility = View.VISIBLE
            val tagView = layoutInflater.inflate(R.layout.fps_section_tagview, null, false)

            val tagTextView = tagView.findViewById(R.id.tagTextView) as TextView
            tagTextView.text = it
            sectionTagLayout.addView(tagView)
        }

    }

    override fun getItemViewId(): Int {
        return JankInfoData.VIEW_ID
    }
}