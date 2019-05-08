package com.silencedut.fpsviewer;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.Choreographer;
import com.silencedut.fpsviewer.background.BackgroundCallback;


import java.util.ArrayList;
import java.util.List;

import static com.silencedut.fpsviewer.utilities.FpsConstants.*;

/**
 * @author SilenceDut
 * @date 2019/3/18
 */
public class FpsEventRelay implements BackgroundCallback {

    private long mLastFrameTimeNanos;
    private boolean mIsStarted;
    private int mFrameIndex;

    private List<FrameListener> mFrameListeners = new ArrayList<>();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            if (mLastFrameTimeNanos != 0) {

                long diffFrameCoast = frameTimeNanos - mLastFrameTimeNanos;

                int skipped = (int) (diffFrameCoast / FRAME_INTERVAL_NANOS - 1);

                byte fps = (byte) (FPS_MAX_DEFAULT - skipped > 0 ? FPS_MAX_DEFAULT - skipped : 0);

                if (mFrameIndex >= Integer.MAX_VALUE) {
                    mFrameIndex = 0;
                }

                for (FrameListener frameListener : mFrameListeners) {
                    frameListener.onFrame(mFrameIndex, skipped, (int) (diffFrameCoast / NANOS_PER_MS), fps);
                }
                mFrameIndex ++;
            }

            mLastFrameTimeNanos = frameTimeNanos;
            Choreographer.getInstance().postFrameCallback(this);
        }
    };

    public int currentFrameIndex() {
        return mFrameIndex;
    }


    void recordFps(boolean start) {
        if (start == mIsStarted) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            for (FrameListener frameListener : mFrameListeners) {
                frameListener.onRecord(start);
            }
            if (start) {
                mLastFrameTimeNanos = 0;
                Choreographer.getInstance().postFrameCallback(frameCallback);
            } else {
                Choreographer.getInstance().removeFrameCallback(frameCallback);
            }
            mIsStarted = start;
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
        void onFrame(int frameIndex, int skipped, int frameCostMillis, byte fps);

        void onRecord(boolean recording);
    }


}
