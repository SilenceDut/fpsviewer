package com.silencedut.fpsviewer.api

import com.silencedut.fpsviewer.transfer.ITransfer

/**
 * @author SilenceDut
 * @date 2019-05-14
 */
interface IEventRelay : ITransfer {

    fun recordFps(start: Boolean)

    fun addFrameListener(frameListener: FrameListener)

    fun currentFrameIndex():Int

    interface FrameListener {
        fun onFrame(frameIndex: Int,  frameCostMillis: Int)

        fun onRecord(recording: Boolean)
    }
}