package com.silencedut.fpsviewersample;

import android.app.Application;
import com.silencedut.fpsviewer.FpsConfig;
import com.silencedut.fpsviewer.FpsViewer;


/**
 * @author SilenceDut
 * @date 2019/3/31
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FpsViewer.getInstance().init(this,FpsConfig.defaultConfig());
    }
}
