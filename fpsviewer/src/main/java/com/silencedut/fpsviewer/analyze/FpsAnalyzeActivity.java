package com.silencedut.fpsviewer.analyze;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.silencedut.fpsviewer.FpsViewer;
import com.silencedut.fpsviewer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SilenceDut
 * @date 2019/3/20
 */
public class FpsAnalyzeActivity extends AppCompatActivity {
    private static final String TAG = "FpsAnalyzeActivity";
    public static final String FPS_BUFFER = "FPS_BUFFER";
    private LineChart mFpsChart;
    private LineChart mKippedChart;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fps_analyze);
        mFpsChart = findViewById(R.id.fps_chart);
        mKippedChart = findViewById(R.id.skipped_frame_chart);
        processData(getIntent());


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processData(intent);
    }

    private void processData(Intent intent) {
        if(intent==null) {
            return;
        }

        int[] fpsBuffer = intent.getIntArrayExtra(FPS_BUFFER);


        Log.i(TAG,"buffer length "+fpsBuffer.length);


        List<Entry> fpsEntries = new ArrayList<>();
        List<Entry> skippedFrameEntries = new ArrayList<>();

        int count = FpsViewer.fpsConfig().getFpsSampleFrameCount();
        int sampleSeconds = FpsViewer.fpsConfig().getFpsSampleMillSeconds();
        int tempFps=0;
        int tempSkips=0;
        for (int index =0 ; index < fpsBuffer.length ; index++) {

            int fps = fpsBuffer[index] >>> 26  ;
            int skipped = fpsBuffer[index] & 0x3FFFFFF ;

            Log.i(TAG,"fps "+fps + "skipped "+skipped);

            fpsEntries.add(new Entry(index , fps));
            skippedFrameEntries.add(new Entry(index , skipped));
//
//            if(index % count !=0 || index == 0) {
//                tempFps += fps;
//                tempSkips += skipped;
//            }else {
//                SentryHelper.info(TAG,"tempFps "+tempFps + "tempSkips "+tempSkips);
//                fpsEntries.add(new Entry(index*sampleSeconds/count , tempFps/count));
//                skippedFrameEntries.add(new Entry(index*sampleSeconds/count , tempSkips/count));
//                tempFps = fps ;
//                tempSkips = skipped ;
//            }

        }

        if(fpsEntries.size() > 0) {
            LineDataSet dataSet = new LineDataSet(fpsEntries, "FPS");
            dataSet.setColor(Color.parseColor("#673ab7"));
            dataSet.disableDashedLine();
            dataSet.setDrawCircles(false);
            dataSet.setValueTextSize(0);

            LineData lineData = new LineData(dataSet);
            mFpsChart.setData(lineData);
            mFpsChart.invalidate();
        }

        if(skippedFrameEntries.size() > 0) {
            LineDataSet dataSet = new LineDataSet(skippedFrameEntries, "Skipped");
            dataSet.setColor(Color.parseColor("#5677fc"));
            dataSet.setValueTextColor(Color.GREEN);

            LineData lineData = new LineData(dataSet);
            mKippedChart.setData(lineData);
            mKippedChart.invalidate();
        }
    }

    @Override
    protected void onDestroy() {
        FpsViewer.fpsDisplayView().initial();
        super.onDestroy();
    }
}
