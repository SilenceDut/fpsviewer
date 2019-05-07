package com.silencedut.fpsviewer;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * @author SilenceDut
 * @date 2019/4/13
 */
public class FpsHelper {




    public static String traceToString(int skipStackCount,Object[] stackArray) {
        if (stackArray == null) {
            return "null";
        }

        if (stackArray.length == 0) {
            return "[]";
        }

        StringBuilder b = new StringBuilder();
        for (int i = 0; i < stackArray.length - skipStackCount ; i++) {
            if (i == stackArray.length - skipStackCount-1) {
                return b.toString();
            }
            b.append(String.valueOf(stackArray[i]));
            b.append("\n");
        }
        return b.toString();
    }
}
