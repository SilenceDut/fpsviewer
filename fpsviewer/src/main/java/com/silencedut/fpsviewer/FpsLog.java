package com.silencedut.fpsviewer;

import android.util.Log;

/**
 * @author SilenceDut
 * @date 2019/3/31
 */
public class FpsLog {
    private static final String TAG = "FpsLog";

    public static void info(String info) {
        Log.i(TAG,info);
    }

    public static void error(String error) {
        Log.e(TAG,error);
    }
}
