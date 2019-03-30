package com.silencedut.fpsviewer;

import android.app.Application;

/**
 * @author SilenceDut
 * @date 2019/3/19
 */
public class FpsViewer {

    private FpsMonitor mFpsMonitor;

    private FpsConfig mFpsConfig;


    private static volatile FpsViewer sFpsViewer;

    private static FpsViewer getInstance() {
        if(sFpsViewer == null) {
            synchronized (FpsViewer.class) {
                if(sFpsViewer == null) {
                    sFpsViewer = new FpsViewer();
                }
            }
        }
        return sFpsViewer;
    }

    public static void init(Application application,FpsConfig fpsConfig){

    }

    public static FpsMonitor getFpsMonitor() {
        return getInstance().mFpsMonitor;
    }

    public static FpsConfig getFpsConfig() {
        return getInstance().mFpsConfig;
    }



}
