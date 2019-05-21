package com.silencedut.fpsviewer;

import android.app.Application;

/**
 * @author SilenceDut
 * @date 2019/5/7
 */
public class FpsViewer implements IViewer{

    public static IViewer getViewer() {
        return  new FpsViewer();
    }

    @Override
    public void initViewer(Application application, FpsConfig fpsConfig) {

    }

    @Override
    public FpsConfig fpsConfig() {
        return null;
    }

    @Override
    public void appendSection(String sectionName) {

    }

    @Override
    public void removeSection(String sectionName) {

    }
}
