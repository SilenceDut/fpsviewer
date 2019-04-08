package com.silencedut.fpsviewersample;

import android.app.Application;
import com.silencedut.fpsviewer.FpsConfig;
import com.silencedut.fpsviewer.FpsViewer;
import com.squareup.leakcanary.LeakCanary;


/**
 * @author SilenceDut
 * @date 2019/3/31
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FpsViewer.getInstance().init(this,FpsConfig.defaultConfig());
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }
}
