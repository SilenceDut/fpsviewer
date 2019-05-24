package com.silencedut.fpsviewer

import android.annotation.TargetApi
import android.os.Build
import android.support.v4.os.TraceCompat
import android.view.Choreographer
import com.silencedut.fpsviewer.api.IEventRelay
import com.silencedut.fpsviewer.background.Background
import com.silencedut.fpsviewer.background.BackgroundCallback
import com.silencedut.fpsviewer.utilities.FpsConstants
import com.silencedut.hub_annotation.HubInject
import java.util.ArrayList

/**
 * @author SilenceDut
 * @date 2019-05-14
 */
@HubInject(api = [IEventRelay::class])
class EventRelayImpl : IEventRelay,BackgroundCallback{
    override fun onCreate() {
        Background.registerBackgroundCallback(this)
    }

    private var mLastFrameTimeNanos: Long = 0
    private var mIsStarted: Boolean = false

    private val mFrameListeners = ArrayList<IEventRelay.FrameListener>()

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (mLastFrameTimeNanos != 0L) {

                val diffFrameCoast = (frameTimeNanos - mLastFrameTimeNanos)/FpsConstants.NANOS_PER_MS

                for (frameListener in mFrameListeners) {
                    frameListener.onFrame(System.currentTimeMillis(), diffFrameCoast.toInt())
                }
            }

            mLastFrameTimeNanos = frameTimeNanos
            Choreographer.getInstance().postFrameCallback(this)
        }
    }


    override fun recordFps(start: Boolean) {
        if (start == mIsStarted) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            for (frameListener in mFrameListeners) {
                frameListener.onRecord(start)
            }
            if (start) {
                mLastFrameTimeNanos = 0
                Choreographer.getInstance().postFrameCallback(frameCallback)
            } else {
                Choreographer.getInstance().removeFrameCallback(frameCallback)
            }
            mIsStarted = start
        }
    }

    override fun addFrameListener(frameListener: IEventRelay.FrameListener) {
        mFrameListeners.add(frameListener)
    }

    override fun onBack2foreground() {
        recordFps(true)
    }

    override fun onFore2background() {
        recordFps(false)
    }



}