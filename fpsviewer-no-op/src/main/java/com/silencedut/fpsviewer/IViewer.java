package com.silencedut.fpsviewer;

import android.app.Application;

/**
 * @author SilenceDut
 * @date 2019-05-21
 */
public interface IViewer {
    void initViewer(Application application, FpsConfig fpsConfig);
    FpsConfig fpsConfig();
    void appendSection(String sectionName);
    void removeSection(String sectionName);
}
