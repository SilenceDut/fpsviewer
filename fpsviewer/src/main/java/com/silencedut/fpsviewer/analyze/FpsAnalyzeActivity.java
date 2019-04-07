package com.silencedut.fpsviewer.analyze;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.PercentFormatter;

import com.silencedut.fpsviewer.FpsConstants;
import com.silencedut.fpsviewer.FpsLog;
import com.silencedut.fpsviewer.FpsViewer;
import com.silencedut.fpsviewer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static final String FPS_BUFFER_INDEX = "FPS_BUFFER_INDEX";
    private LineChart mFpsChart;
    private PieChart mPieChart;

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
        mPieChart = findViewById(R.id.fps_level_pieChart);
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


        FpsLog.info( "buffer length "+fpsBuffer.length);


        List<Entry> fpsEntries = new ArrayList<>();


        int temTotalCost =0;

        int aLevelDurations =0;
        int bLevelDurations =0;
        int cLevelDurations =0;
        int dLevelDurations = 0;
        int frozenDurations =0;

        for (int perFrameCost : fpsBuffer) {

            temTotalCost += perFrameCost;
            int skipped = (int) ((perFrameCost*FpsConstants.NANOS_PER_MS - FRAME_INTERVAL_NANOS) / FRAME_INTERVAL_NANOS);

            if(skipped < 0 ) {
                FpsLog.info("perFrameCost " + perFrameCost);
            }

            int fps = Math.max(0,Math.min(FPS_MAX_DEFAULT - skipped ,60));

            fpsEntries.add(new Entry(temTotalCost, fps));


            if(skipped >= 60) {
                frozenDurations += perFrameCost;
            }else if(skipped >= 50) {
                dLevelDurations += perFrameCost;
            }else if(skipped >= 30) {
                cLevelDurations += perFrameCost;
            }else if(skipped >= 10) {
                bLevelDurations += perFrameCost;
            }else if(skipped >= 0) {
                aLevelDurations += perFrameCost;
            }

        }

        List<PieEntry> levelPieEntries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        filterPieEntry(levelPieEntries,colors,frozenDurations,"> 60",Color.RED,temTotalCost);
        filterPieEntry(levelPieEntries,colors,dLevelDurations,"50~60",Color.YELLOW,temTotalCost);
        filterPieEntry(levelPieEntries,colors,cLevelDurations,"30~50",Color.parseColor("#FF6100"),temTotalCost);
        filterPieEntry(levelPieEntries,colors,bLevelDurations,"10~30",Color.BLUE,temTotalCost);
        filterPieEntry(levelPieEntries,colors,aLevelDurations,"0~10",Color.parseColor("#3498c2"),temTotalCost);


        showRealRimeFpsChart(fpsEntries,fpsBuffer,temTotalCost);
        showLevelPieChart(levelPieEntries,colors);

    }

    private void filterPieEntry(List<PieEntry> levelPieEntries, List<Integer> colors,int skipDurations,String label,int color,int totalCost) {
        if(skipDurations * 1.0f/ totalCost < 0.01f) {
            return;
        }
        levelPieEntries.add(new PieEntry(skipDurations,label));
        colors.add(color);

    }


    private void showRealRimeFpsChart(List<Entry> fpsEntries ,int[] fpsBuffer , int totalCost) {

        if(fpsEntries.size() > 0) {
            LineDataSet dataSet = new LineDataSet(fpsEntries, "FPS");
            dataSet.setColor(Color.parseColor("#673ab7"));
            dataSet.setDrawCircles(false);
            dataSet.setValueTextSize(0);
            dataSet.setLabel("The average frame rate："+ Math.min(fpsBuffer.length * MS_PER_SECOND/ totalCost,FPS_MAX_DEFAULT));
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

    private void showLevelPieChart(List<PieEntry> levelPieEntries,List<Integer> colors) {
        mPieChart.setUsePercentValues(true);
        mPieChart.getDescription().setEnabled(false);
        mPieChart.setExtraOffsets(5, 10, 5, 5);
        mPieChart.setDrawEntryLabels(false);


        mPieChart.setDrawCenterText(false);

        mPieChart.setDrawHoleEnabled(false);







        mPieChart.setRotationAngle(0);

        mPieChart.setRotationEnabled(false);
        mPieChart.setHighlightPerTapEnabled(true);


        PieDataSet dataSet = new PieDataSet(levelPieEntries, "Skipped frame per second");
        dataSet.setSelectionShift(5f);


        dataSet.setValueLineColor(Color.parseColor("#212121"));
        dataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        dataSet.setSliceSpace(0);

        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        mPieChart.setData(data);
        mPieChart.highlightValues(null);


        //设置数据
        mPieChart.setData(data);

        mPieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        Legend l = mPieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // 输入标签样式
        mPieChart.setEntryLabelColor(Color.WHITE);
        mPieChart.setEntryLabelTextSize(12f);

    }



    @Override
    protected void onDestroy() {
        FpsViewer.fpsDisplayView().initial();
        super.onDestroy();
    }

}
