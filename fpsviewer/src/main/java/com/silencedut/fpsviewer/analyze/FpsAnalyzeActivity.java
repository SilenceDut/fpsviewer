package com.silencedut.fpsviewer.analyze;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.silencedut.fpsviewer.FpsConstants;
import com.silencedut.fpsviewer.FpsViewer;
import com.silencedut.fpsviewer.R;

import java.util.ArrayList;
import java.util.List;

import static com.silencedut.fpsviewer.FpsConstants.FPS_MAX_DEFAULT;
import static com.silencedut.fpsviewer.FpsConstants.FRAME_INTERVAL_NANOS;
import static com.silencedut.fpsviewer.FpsConstants.MS_PER_SECOND;

/**
 * @author SilenceDut
 * @date 2019/3/20
 */
public class FpsAnalyzeActivity extends AppCompatActivity {
    private static final String TAG = "FpsAnalyzeActivity";
    public static final String FPS_BUFFER = "FPS_BUFFER";
    private LineChart mFpsChart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fps_analyze);
       ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mFpsChart = findViewById(R.id.fps_chart);
        processData(getIntent());


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
                default:
        }
        return super.onOptionsItemSelected(item);
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
        int totalCost =0;
        for (int perFrameCost : fpsBuffer) {

            totalCost += perFrameCost;
            int skipped = (int) ((perFrameCost*FpsConstants.NANOS_PER_MS - FRAME_INTERVAL_NANOS) / FRAME_INTERVAL_NANOS);

            int fps = FPS_MAX_DEFAULT - skipped > 0 ? FPS_MAX_DEFAULT - skipped : 0;

            fpsEntries.add(new Entry(totalCost, fps));

        }


        if(fpsEntries.size() > 0) {
            LineDataSet dataSet = new LineDataSet(fpsEntries, "FPS");
            dataSet.setColor(Color.parseColor("#673ab7"));
            dataSet.setDrawCircles(false);
            dataSet.setValueTextSize(0);
            dataSet.setLabel("平均帧率："+Math.min(fpsBuffer.length * MS_PER_SECOND/ totalCost,FPS_MAX_DEFAULT));
            dataSet.setDrawFilled(false);


            LineData lineData = new LineData(dataSet);
            mFpsChart.setData(lineData);
            mFpsChart.setDragEnabled(false);
            mFpsChart.setDoubleTapToZoomEnabled(false);
            mFpsChart.setTouchEnabled(false);


            Description description = new Description();
            description.setText("毫秒(ms)");

            mFpsChart.setDescription(description);

            XAxis xAxis = mFpsChart.getXAxis();
            xAxis.setDrawGridLines(false);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(true);
            xAxis.setAxisLineWidth(1);

            YAxis leftAxis = mFpsChart.getAxisLeft();
            leftAxis.enableGridDashedLine(10f, 10f, 0f);
            leftAxis.setDrawZeroLine(false);
            leftAxis.setDrawGridLines(true);
            mFpsChart.getAxisRight().setEnabled(false);
            mFpsChart.getLegend().setDrawInside(true);
            mFpsChart.getLegend().setYOffset(20);
            mFpsChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            mFpsChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            mFpsChart.setExtraOffsets(10, 10, 10, 0);

        }

    }

    @Override
    protected void onDestroy() {
        FpsViewer.fpsDisplayView().initial();
        super.onDestroy();
    }
}
