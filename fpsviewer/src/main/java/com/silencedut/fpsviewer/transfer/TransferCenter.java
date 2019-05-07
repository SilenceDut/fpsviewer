package com.silencedut.fpsviewer.transfer;

import com.silencedut.hub.Hub;
import com.silencedut.hub.IHub;

/**
 * @author SilenceDut
 * @date 2019/5/5
 */
public class TransferCenter {
    public static  <T extends IHub> T getImpl(Class<T> iHub){
        return Hub.getImpl(iHub);
    }
}
