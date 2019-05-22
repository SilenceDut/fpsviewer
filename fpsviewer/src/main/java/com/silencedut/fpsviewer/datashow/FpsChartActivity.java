package com.silencedut.fpsviewer.datashow;

import android.content.Intent;
import android.graphics.Color;

import android.graphics.Typeface;

import android.view.View;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.PercentFormatter;

import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.silencedut.fpsviewer.*;
import com.silencedut.fpsviewer.api.IDisplayFps;
import com.silencedut.fpsviewer.api.INavigator;
import com.silencedut.fpsviewer.data.JankInfo;
import com.silencedut.fpsviewer.jank.IJankRepository;
import com.silencedut.fpsviewer.transfer.TransferCenter;
import com.silencedut.fpsviewer.utilities.FpsConstants;
import com.silencedut.fpsviewer.utilities.FpsLog;

import java.util.ArrayList;
import java.util.List;

import static com.silencedut.fpsviewer.utilities.FpsConstants.*;
import static com.silencedut.fpsviewer.utilities.FpsConstants.NANOS_PER_MS;

/**
 * @author SilenceDut
 * @date 2019/3/20
 */
public class FpsChartActivity extends BaseFpsViewerActivity {
    private static final String TAG = "FpsAnalyzeActivity";
    private FpsConfig mFpsConfig = TransferCenter.getImpl(IViewer.class).fpsConfig();
    private LineChart mFpsChart;
    private PieChart mPieChart;
    private View mPeriodJank;

    @Override
    public int provideContentViewId() {
        return R.layout.fps_activity_chart;
    }

    @Override
    public void initViews() {
        mFpsChart = findViewById(R.id.fps_chart);
        mPieChart = findViewById(R.id.fps_level_pieChart);
        mPeriodJank = findViewById(R.id.period_jank);
        mPeriodJank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransferCenter.getImpl(INavigator.class).toJankInfosActivity(FpsChartActivity.this);
            }
        });
        processData(getIntent());
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processData(intent);
    }


    private void processData(Intent intent) {
        if (intent == null) {
            return;
        }

        long[] fpsBuffer = TransferCenter.getImpl(IDisplayFps.class).recordDatas();

        List<Entry> fpsEntries = new ArrayList<>();

        int temTotalCost = 0;

        int aLevelDurations = 0;
        int bLevelDurations = 0;
        int cLevelDurations = 0;
        int dLevelDurations = 0;
        int frozenDurations = 0;

        for (long record : fpsBuffer) {

            int perFrameCost = TransferCenter.getImpl(IDisplayFps.class).frameCostMillis(record);

            temTotalCost += perFrameCost;

            int skipped = (int) ((perFrameCost * NANOS_PER_MS - FRAME_INTERVAL_NANOS) / FRAME_INTERVAL_NANOS);

            int fps = Math.max(0, Math.min(FPS_MAX_DEFAULT - skipped, 60));

            fpsEntries.add(new Entry(temTotalCost, fps, TransferCenter.getImpl(IDisplayFps.class).frameTimeMillis(record)));

            if (skipped >= 60) {
                frozenDurations += perFrameCost;
            } else if (skipped >= 50) {
                dLevelDurations += perFrameCost;
            } else if (skipped >= 30) {
                cLevelDurations += perFrameCost;
            } else if (skipped >= 10) {
                bLevelDurations += perFrameCost;
            } else if (skipped >= 0) {
                aLevelDurations += perFrameCost;
            }
        }
        showRealRimeFpsChart(fpsEntries, fpsBuffer, temTotalCost);

        List<PieEntry> levelPieEntries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        filterPieEntry(levelPieEntries, colors, frozenDurations, "> 60", Color.parseColor("#ff5177"), temTotalCost);
        filterPieEntry(levelPieEntries, colors, dLevelDurations, "50~60", Color.parseColor("#ff9800"), temTotalCost);
        filterPieEntry(levelPieEntries, colors, cLevelDurations, "30~50", Color.parseColor("#FF6100"), temTotalCost);
        filterPieEntry(levelPieEntries, colors, bLevelDurations, "10~30", Color.parseColor("#5677fc"), temTotalCost);
        filterPieEntry(levelPieEntries, colors, aLevelDurations, "0~10", Color.parseColor("#3498c2"), temTotalCost);

        showLevelPieChart(levelPieEntries, colors);

    }

    private void filterPieEntry(List<PieEntry> levelPieEntries, List<Integer> colors, int skipDurations, String label, int color, int totalCost) {
        if (skipDurations * 1.0f / totalCost < 0.01f) {
            return;
        }
        levelPieEntries.add(new PieEntry(skipDurations, label));
        colors.add(color);

    }


    private void showRealRimeFpsChart(List<Entry> fpsEntries, long[] fpsBuffer, int totalCost) {

        if (fpsEntries.size() > 0) {
            final LineDataSet dataSet = new LineDataSet(fpsEntries, "FPS");
            dataSet.setColor(Color.parseColor("#673ab7"));
            dataSet.setDrawCircles(false);
            dataSet.setValueTextSize(0);

            String label = getString(R.string.average_fps);
            dataSet.setLabel(label + Math.min(fpsBuffer.length * MS_PER_SECOND / totalCost, FPS_MAX_DEFAULT));
            dataSet.setDrawFilled(false);


            dataSet.setHighlightEnabled(true);
            dataSet.setDrawVerticalHighlightIndicator(false);
            dataSet.setDrawHorizontalHighlightIndicator(false);

            final LineData lineData = new LineData(dataSet);

            mFpsChart.setData(lineData);
            mFpsChart.setDragEnabled(true);
            mFpsChart.setDoubleTapToZoomEnabled(true);
            mFpsChart.setTouchEnabled(true);

            mFpsChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    long jankPoint = (long) e.getData();
                    JankInfo jankInfo = TransferCenter.getImpl(IJankRepository.class).jankDetailByPointData(jankPoint).getValue();
                    if(canShowDetails(e.getY()) && jankInfo!=null) {
                        TransferCenter.getImpl(INavigator.class).toJankDetailsActivity(FpsChartActivity.this,jankInfo.getOccurredTime());
                    }
                    FpsLog.info("onValueSelected " + e);
                }

                @Override
                public void onNothingSelected() {
                    FpsLog.info("onNothingSelected ");
                }
            });


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
            mFpsChart.getLegend().setTextSize(14);
            mFpsChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            mFpsChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            mFpsChart.setExtraOffsets(10, 10, 10, 0);

        }
    }

    private boolean canShowDetails(float fps) {
        FpsLog.info("canShowMarker "+fps+",,"+(FPS_MAX_DEFAULT - mFpsConfig.getJankThreshold()*NANOS_PER_MS/ FpsConstants.FRAME_INTERVAL_NANOS));
        return fps < (FPS_MAX_DEFAULT - mFpsConfig.getJankThreshold()*NANOS_PER_MS/ FpsConstants.FRAME_INTERVAL_NANOS);
    }


    private void showLevelPieChart(List<PieEntry> levelPieEntries, List<Integer> colors) {
        mPieChart.setUsePercentValues(true);
        mPieChart.getDescription().setEnabled(false);

        mPieChart.setDrawEntryLabels(false);
        mPieChart.setDragDecelerationFrictionCoef(0.95f);

        mPieChart.setDrawCenterText(true);
        mPieChart.setCenterTextColor(Color.parseColor("#7c4dff"));
        mPieChart.setCenterTextTypeface(Typeface.MONOSPACE);
        mPieChart.setCenterText(getString(R.string.frame_skip));
        mPieChart.setCenterTextSize(16);

        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleColor(Color.WHITE);

        mPieChart.setTransparentCircleColor(Color.WHITE);
        mPieChart.setTransparentCircleAlpha(110);

        mPieChart.setHoleRadius(40f);
        mPieChart.setTransparentCircleRadius(43f);


        mPieChart.setRotationAngle(0);

        mPieChart.setRotationEnabled(true);
        mPieChart.setHighlightPerTapEnabled(true);

        String pieLabel = getString(R.string.average_frame_skip);
        PieDataSet dataSet = new PieDataSet(levelPieEntries, pieLabel);
        dataSet.setValueLineColor(Color.parseColor("#212121"));
        dataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);


        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);

        dataSet.setSelectionShift(5f);

        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(mPieChart));
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);
        mPieChart.setData(data);

        mPieChart.animateY(1400, Easing.EaseInOutQuad);

        Legend l = mPieChart.getLegend();
        l.setTextSize(12);
        l.setXEntrySpace(5f);

    }

}
