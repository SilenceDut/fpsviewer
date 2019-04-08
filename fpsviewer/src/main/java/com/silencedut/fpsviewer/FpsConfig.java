package com.silencedut.fpsviewer;

import android.os.Process;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author SilenceDut
 * @date 2019/3/26
 */
public class FpsConfig {

    public static FpsConfig defaultConfig(){
        return new Builder().build();
    }

    private boolean fpsViewEnable ;
    private boolean enableOutputFpsData ;
    /**
     * 采样周期,毫秒
     */
    private int fpsSampleMillSeconds ;
    /**
     * 每次采集多少帧
     */
    private int fpsSampleFrameCount ;

    private ExecutorService taskExecutor;

    public boolean isFpsViewEnable() {
        return fpsViewEnable;
    }

    public boolean isEnableOutputFpsData() {
        return enableOutputFpsData;
    }

    public int getFpsSampleMillSeconds() {
        return fpsSampleMillSeconds;
    }

    public int getFpsSampleFrameCount() {
        return fpsSampleFrameCount;
    }

    public ExecutorService getTaskExecutor() {
        return taskExecutor;
    }

    private FpsConfig(Builder builder) {
        this.fpsViewEnable = builder.fpsViewEnable;
        this.taskExecutor = builder.taskExecutor;
    }


    public static final class Builder {
        private boolean fpsViewEnable = true;
        private boolean enableOutputFpsData = true;

        ExecutorService taskExecutor;
        Builder() {

        }

        public Builder fpsViewEnable(boolean enable){
            this.fpsViewEnable  = enable;
            return this;
        }

        public Builder enableOutputFpsData(boolean enableOutputFpsData){
            this.enableOutputFpsData  = enableOutputFpsData;
            return this;
        }

        public Builder fpsSamplePeriod(int millSeconds){
            return this;
        }

        public Builder fpsSampleFrameCount(int count){
            return this;
        }

        public Builder providerExecutor(ExecutorService taskExecutor) {
            this.taskExecutor = taskExecutor;
            return this;
        }

        public FpsConfig build() {
            if(taskExecutor == null) {
                taskExecutor = new ThreadPoolExecutor(2, 2,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>(),new FpsThreadFactory());
            }
            return new FpsConfig(this);
        }
    }

    private static class FpsThreadFactory implements ThreadFactory {

        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "FpsThread #" + mCount.getAndIncrement());
            thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
            return thread;
        }
    }

}
