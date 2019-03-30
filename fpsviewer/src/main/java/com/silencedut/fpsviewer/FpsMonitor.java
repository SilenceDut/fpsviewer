package com.silencedut.fpsviewer;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.Choreographer;


import java.util.ArrayList;
import java.util.List;

import static com.silencedut.fpsviewer.FpsConstants.FPS_MAX_DEFAULT;
import static com.silencedut.fpsviewer.FpsConstants.FRAME_INTERVAL_NANOS;

/**
 * @author SilenceDut
 * @date 2019/3/18
 */
public class FpsMonitor {
    private static final String TAG_SUFFIX = "FpsMonitor";

    private long mLastFrameTimeNanos;

    private List<FrameListener> mFrameListeners = new ArrayList<>();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            if(mLastFrameTimeNanos!=0) {

                long diff = frameTimeNanos - mLastFrameTimeNanos;

                int skipped = (int)((diff - FRAME_INTERVAL_NANOS) / FRAME_INTERVAL_NANOS);

                byte fps = (byte) (FPS_MAX_DEFAULT - skipped > 0 ? FPS_MAX_DEFAULT - skipped : 0);

                for(FrameListener frameListener : mFrameListeners) {
                    frameListener.onFrame(fps,skipped,diff);
                }
            }

            mLastFrameTimeNanos = frameTimeNanos;
            Choreographer.getInstance().postFrameCallback(this);
        }
    };


    public void recordFps(boolean start) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if(start) {
                mLastFrameTimeNanos = 0;

                Choreographer.getInstance().postFrameCallback(frameCallback);
            } else {
                Choreographer.getInstance().removeFrameCallback(frameCallback);
            }
        }
    }

    public void addFrameListener(FrameListener frameListener) {
        mFrameListeners.add(frameListener);
    }

    public interface FrameListener {
        void onFrame(byte fps, int skipped, long frameCostMillis);
    }


}
