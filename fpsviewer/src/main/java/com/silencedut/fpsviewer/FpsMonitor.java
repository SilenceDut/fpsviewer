package com.silencedut.fpsviewer;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.Choreographer;
import com.duowan.makefriends.framework.context.BackgroundCallback;


import java.util.ArrayList;
import java.util.List;

import static com.silencedut.fpsviewer.FpsConstants.*;

/**
 * @author SilenceDut
 * @date 2019/3/18
 */
class FpsMonitor implements BackgroundCallback {
    private static final String TAG_SUFFIX = "FpsMonitor";

    private long mLastFrameTimeNanos;
    private boolean isStarted;

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
                    frameListener.onFrame(fps,skipped, (int) (diff/ NANOS_PER_MS));
                }
            }

            mLastFrameTimeNanos = frameTimeNanos;
            Choreographer.getInstance().postFrameCallback(this);
        }
    };


    void recordFps(boolean start) {
        if(start == isStarted) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            for(FrameListener frameListener : mFrameListeners) {
                frameListener.onRecord(start);
            }
            if(start) {
                mLastFrameTimeNanos = 0;
                Choreographer.getInstance().postFrameCallback(frameCallback);
            } else {
                Choreographer.getInstance().removeFrameCallback(frameCallback);
            }
            isStarted = start;
        }
    }

    public void addFrameListener(FrameListener frameListener) {
        mFrameListeners.add(frameListener);
    }

    @Override
    public void onBack2foreground() {
        recordFps(true);
    }

    @Override
    public void onFore2background() {
        recordFps(false);
    }

    public interface FrameListener {
        void onFrame(byte fps, int skipped, int frameCostMillis);
        void onRecord(boolean recording);
    }


}
