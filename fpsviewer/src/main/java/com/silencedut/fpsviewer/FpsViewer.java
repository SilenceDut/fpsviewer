package com.silencedut.fpsviewer;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import com.silencedut.fpsviewer.background.Background;
import com.silencedut.fpsviewer.sniper.MainThreadJankSniper;

/**
 * @author SilenceDut
 * @date 2019/3/19
 */
public class FpsViewer {

    private FpsEventRelay mFpsEventCenter;

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
        mFpsEventCenter = new FpsEventRelay();
        if(this.mFpsConfig.isFpsViewEnable()) {
            FpsViewer.mainHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDisplayView = DisplayView.create(application).prepare();
                    MainThreadJankSniper.start();
                    FpsViewer.fpsEventRelay().recordFps(true);
                    Background.INSTANCE.registerBackgroundCallback(mFpsEventCenter);
                }
            },3000);
        }



    }

    public static FpsEventRelay fpsEventRelay() {
        return getInstance().mFpsEventCenter;
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
