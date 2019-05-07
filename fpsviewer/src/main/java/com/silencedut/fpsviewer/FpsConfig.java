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
     * 获取主线程的执行栈周期,毫秒
     */
    private int traceSamplePeriod ;
    /**
     * 卡顿阈值,毫秒
     */
    private int jankThreshold;

    private ExecutorService taskExecutor;

    public boolean isFpsViewEnable() {
        return fpsViewEnable;
    }

    public boolean isEnableOutputFpsData() {
        return enableOutputFpsData;
    }

    public int getTraceSamplePeriod() {
        return traceSamplePeriod;
    }

    public int getJankThreshold() {
        return jankThreshold;
    }

    public ExecutorService getTaskExecutor() {
        return taskExecutor;
    }

    private FpsConfig(Builder builder) {
        this.fpsViewEnable = builder.fpsViewEnable;
        this.taskExecutor = builder.taskExecutor;
        this.enableOutputFpsData = builder.enableOutputFpsData;
        this.jankThreshold = builder.jankThreshold;
        this.traceSamplePeriod = builder.jankThreshold;
    }


    public static final class Builder {
        boolean fpsViewEnable = true;
        boolean enableOutputFpsData = true;
        /**
         * 采样周期,毫秒
         */
        int traceSamplePeriod = 50;
        /**
         * 卡顿阈值,毫秒
         */
        int jankThreshold = 120;

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

        public Builder traceSamplePeriod(int traceSamplePeriod){
            this.traceSamplePeriod = traceSamplePeriod;
            return this;
        }

        public Builder jankThreshold(int jankThreshold){
            this.jankThreshold = jankThreshold;
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
                        new LinkedBlockingQueue<Runnable>(128),new FpsThreadFactory());
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
