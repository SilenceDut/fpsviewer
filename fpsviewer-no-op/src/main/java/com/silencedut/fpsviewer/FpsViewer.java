package com.silencedut.fpsviewer;

import android.app.Application;

/**
 * @author SilenceDut
 * @date 2019/5/7
 */
public class FpsViewer {
    public static FpsViewer getInstance() {
        return new FpsViewer();
    }

    public void init(final Application application,  FpsConfig fpsConfig){
    }
}
