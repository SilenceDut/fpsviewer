package com.silencedut.fpsviewer.utilities;

/**
 * @author SilenceDut
 * @date 2019/3/25
 */
public class FpsConstants {

     public static final short FPS_MAX_DEFAULT = 60;
     /**
      * at least 10 minute
      */
     public static final int FPS_MAX_COUNT_DEFAULT = 100000;
     public static final double FRAME_INTERVAL_NANOS = Math.pow(10,9)/ 60;
     public static final int NANOS_PER_MS = 1000000;
     public static final int MS_PER_SECOND = 1000;

     public static final String DATABASE_NAME = "fpsviewer-db";

}
