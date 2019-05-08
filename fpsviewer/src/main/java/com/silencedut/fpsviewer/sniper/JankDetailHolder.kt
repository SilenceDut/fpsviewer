package com.silencedut.fpsviewer.sniper

import android.view.View
import android.widget.TextView
import com.silencedut.diffadapter.DiffAdapter
import com.silencedut.diffadapter.holder.BaseDiffViewHolder
import com.silencedut.fpsviewer.R

/**
 * @author SilenceDut
 * @date 2019/5/6
 */
class JankDetailHolder(itemView: View, diffAdapter: DiffAdapter) : BaseDiffViewHolder<JankDetailData>(itemView,diffAdapter) {
    var countTv:TextView = itemView.findViewById(R.id.count)
    var stackTv:TextView = itemView.findViewById(R.id.stack_detail)

    override fun updateItem(data: JankDetailData, position: Int) {
        countTv.text = data.count.toString()
        stackTv.text = data.stack
    }

    override fun getItemViewId(): Int {
        return JankDetailData.VIEW_ID
    }


}