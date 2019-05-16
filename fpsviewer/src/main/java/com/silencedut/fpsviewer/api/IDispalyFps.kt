package com.silencedut.fpsviewer.api

import com.silencedut.fpsviewer.transfer.ITransfer

/**
 * @author SilenceDut
 * @date 2019-05-14
 */
interface IDisplayFps : ITransfer {
    /**
     * show or dismiss fps viewer
     */
    fun dismiss()

    fun startUpdate()

    fun buildRecord(frameTimeMillis: Long, fps: Int): Long

    fun frameTimeMillis(record: Long): Long

    fun frameCostMillis(record: Long): Int

    fun recordDatas():LongArray

    fun periodStartTime() : Long

    fun buildDisplayStack(push: Boolean)
}