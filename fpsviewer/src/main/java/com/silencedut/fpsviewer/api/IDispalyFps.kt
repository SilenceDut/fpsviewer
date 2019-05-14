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
    fun show()
}