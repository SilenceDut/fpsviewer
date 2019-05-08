package com.silencedut.fpsviewer.sniper;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import androidx.annotation.WorkerThread;
import com.silencedut.fpsviewer.data.IJankRepository;
import com.silencedut.fpsviewer.utilities.FpsHelper;
import com.silencedut.fpsviewer.utilities.FpsLog;
import com.silencedut.fpsviewer.FpsEventRelay;
import com.silencedut.fpsviewer.FpsViewer;
import com.silencedut.fpsviewer.transfer.TransferCenter;

import java.util.*;

/**
 * 获取卡顿时主线程的堆栈
 * @author SilenceDut
 * @date 2019/4/11
 */
public class MainThreadJankSniper implements FpsEventRelay.FrameListener {
    private static final int METHOD_TRACE_SKIP = 2;
    private Handler mSampleHandler;
    private List<StackTraceElement[]> mTracesInOneFrame= new ArrayList<>();
    private boolean mIsRecording;
    private IJankRepository mJankRepository = TransferCenter.getImpl(IJankRepository.class);

    private Runnable mSampleTask = new Runnable() {
        @Override
        public void run() {

            takeMainThreadSnapshot();

            mSampleHandler.postDelayed(this,FpsViewer.fpsConfig().getTraceSamplePeriod());
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
        FpsViewer.fpsEventRelay().addFrameListener(this);
    }

    public static void start() {
        new MainThreadJankSniper();
    }


    @Override
    public void onFrame(int frameIndex, int skipped, int frameCostMillis, byte fps) {
        if(this.mIsRecording) {
            mSampleHandler.removeCallbacks(mSampleTask);
            dealPreFrameTraceInfo(frameIndex,frameCostMillis);
            mSampleHandler.postDelayed(mSampleTask,FpsViewer.fpsConfig().getTraceSamplePeriod());
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

                    if(frameCostMillis > FpsViewer.fpsConfig().getJankThreshold() && mTracesInOneFrame.size()>0) {

                        Map<String, Integer> stackCountMap = new HashMap<>(16);
                        String traceStr;
                        for (StackTraceElement[] trace : mTracesInOneFrame) {
                            traceStr = FpsHelper.traceToString(METHOD_TRACE_SKIP, trace);
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
