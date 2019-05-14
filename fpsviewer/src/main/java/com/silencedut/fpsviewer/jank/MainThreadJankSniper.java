package com.silencedut.fpsviewer.jank;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import androidx.annotation.WorkerThread;

import com.silencedut.fpsviewer.FpsConfig;
import com.silencedut.fpsviewer.IViewer;
import com.silencedut.fpsviewer.api.IEventRelay;
import com.silencedut.fpsviewer.api.IUtilities;
import com.silencedut.fpsviewer.utilities.FpsLog;
import com.silencedut.fpsviewer.transfer.TransferCenter;

import java.util.*;

/**
 * 获取卡顿时主线程的堆栈
 * @author SilenceDut
 * @date 2019/4/11
 */
public class MainThreadJankSniper implements IEventRelay.FrameListener {
    private static final int METHOD_TRACE_SKIP = 2;
    private Handler mSampleHandler;
    private List<StackTraceElement[]> mTracesInOneFrame= new ArrayList<>();
    private boolean mIsRecording;
    private IJankRepository mJankRepository = TransferCenter.getImpl(IJankRepository.class);
    private FpsConfig mFpsConfig = TransferCenter.getImpl(IViewer.class).fpsConfig();

    private Runnable mSampleTask = new Runnable() {
        @Override
        public void run() {

            takeMainThreadSnapshot();

            mSampleHandler.postDelayed(this,mFpsConfig.getTraceSamplePeriod());
        }
    };

    @WorkerThread
    private void takeMainThreadSnapshot(){
        Thread mainThread = Looper.getMainLooper().getThread();
        StackTraceElement[] stackArray = mainThread.getStackTrace();
        mTracesInOneFrame.add(stackArray);
    }

    private MainThreadJankSniper() {

        HandlerThread sampleThread = new HandlerThread("trace_sample");
        sampleThread.start();
        mSampleHandler = new Handler(sampleThread.getLooper());
        TransferCenter.getImpl(IEventRelay.class).addFrameListener(this);
    }

    public static void prepare() {
        new MainThreadJankSniper();
    }


    @Override
    public void onFrame(int frameIndex, int frameCostMillis) {
        if(this.mIsRecording) {
            mSampleHandler.removeCallbacks(mSampleTask);
            dealPreFrameTraceInfo(frameIndex,frameCostMillis);
            mSampleHandler.postDelayed(mSampleTask,mFpsConfig.getTraceSamplePeriod());
        }
    }

    @Override
    public void onRecord(boolean recording) {
        this.mIsRecording = recording;
    }

    private void dealPreFrameTraceInfo(final int frameIndex,final int frameCostMillis) {

            mSampleHandler.post(new Runnable() {
                @Override
                public void run() {

                    if(frameCostMillis > mFpsConfig.getJankThreshold() && mTracesInOneFrame.size()>0) {

                        Map<String, Integer> stackCountMap = new HashMap<>(16);
                        String traceStr;
                        for (StackTraceElement[] trace : mTracesInOneFrame) {
                            traceStr = TransferCenter.getImpl(IUtilities.class).traceToString(METHOD_TRACE_SKIP, trace);
                            Integer count = stackCountMap.get(traceStr);
                            FpsLog.info("\n hashcode:" + traceStr.hashCode() + "\n trace:" + traceStr);
                            if (null != count) {
                                stackCountMap.put(traceStr, count + 1);
                            } else {
                                stackCountMap.put(traceStr, 1);
                            }
                        }

                        List<Map.Entry<String, Integer>> stackCountEntries = new ArrayList<>(stackCountMap.entrySet());
                        Collections.sort(stackCountEntries, new Comparator<Map.Entry<String, Integer>>() {
                            @Override
                            public int compare(Map.Entry<String, Integer> arg0, Map.Entry<String, Integer> arg1) {
                                return arg1.getValue().compareTo(arg0.getValue());
                            }
                        });
                        mJankRepository.storeJankTraceInfo(frameIndex,frameCostMillis,stackCountEntries);
                    }
                    mTracesInOneFrame.clear();
                }
            });
    }

}
