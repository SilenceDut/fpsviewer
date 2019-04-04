package com.silencedut.fpsviewer;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import com.silencedut.fpsviewer.background.Background;

/**
 * @author SilenceDut
 * @date 2019/3/19
 */
public class FpsViewer {

    private FpsMonitor mFpsMonitor;

    private FpsConfig mFpsConfig;


    private @Nullable DisplayView mDisplayView;

    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    private static volatile FpsViewer sFpsViewer;

    public static FpsViewer getInstance() {
        if(sFpsViewer == null) {
            synchronized (FpsViewer.class) {
                if(sFpsViewer == null) {
                    sFpsViewer = new FpsViewer();
                }
            }
        }
        return sFpsViewer;
    }


    public  void init(final Application application, @Nullable FpsConfig fpsConfig){
        Background.INSTANCE.init(application);
        if(fpsConfig == null) {
           this.mFpsConfig = FpsConfig.defaultConfig();
        }else {
            this.mFpsConfig = fpsConfig;
        }
        mFpsMonitor = new FpsMonitor();
        if(this.mFpsConfig.isFpsViewEnable()) {
            FpsViewer.mainHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDisplayView = DisplayView.create(application).prepare();
                    FpsViewer.fpsMonitor().recordFps(true);

                    Background.INSTANCE.registerBackgroundCallback(mDisplayView);
                    Background.INSTANCE.registerBackgroundCallback(mFpsMonitor);
                }
            },3000);
        }



    }

    public static FpsMonitor fpsMonitor() {
        return getInstance().mFpsMonitor;
    }

    public static FpsConfig fpsConfig() {
        return getInstance().mFpsConfig;
    }

    public static DisplayView fpsDisplayView() {
        return getInstance().mDisplayView;
    }

    public static Handler mainHandler() {
        return getInstance().mMainHandler;
    }

}
