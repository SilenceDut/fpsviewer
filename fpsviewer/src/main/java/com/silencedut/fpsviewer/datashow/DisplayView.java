package com.silencedut.fpsviewer.datashow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import com.silencedut.fpsviewer.api.INavigator;
import com.silencedut.fpsviewer.R;
import com.silencedut.fpsviewer.api.IDisplayFps;
import com.silencedut.fpsviewer.api.IEventRelay;
import com.silencedut.fpsviewer.api.IUtilities;
import com.silencedut.fpsviewer.transfer.TransferCenter;
import com.silencedut.fpsviewer.utilities.FpsLog;
import com.silencedut.hub_annotation.HubInject;
import org.jetbrains.annotations.NotNull;

import static com.silencedut.fpsviewer.utilities.FpsConstants.*;


/**
 * @author SilenceDut
 * @date 2019/3/26
 */
@HubInject(api = IDisplayFps.class)
public class DisplayView implements IDisplayFps, View.OnClickListener, View.OnTouchListener, IEventRelay.FrameListener {


    enum STATE {
        /**
         * 浮窗View状态
         */
        UPDATE, STOP
    }

    private static final String TAG = "FpsViewer";
    private static final int MOVE_ERROR = 20;
    private static final int FPS_LEVEL_BAD = 15;

    private View mRootView;
    private TextView mFpsTv;
    private View mChart;
    private View mJankStack;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;

    private float mStartPositionX = 0;
    private float mStartPositionY = 0;

    private float mTouchStartX;
    private float mTouchStartY;

    private Drawable mDGradeDrawable;
    private Drawable mAGradeDrawable;

    private STATE mState = STATE.UPDATE;

    private long[] mFrameCostBuffer = new long[FPS_MAX_COUNT_DEFAULT];

    private int mCurrentFrameIndex;

    private int stack;


    @Override
    public void onCreate() {
        TransferCenter.getImpl(IEventRelay.class).addFrameListener(this);
        initView(TransferCenter.getImpl(IUtilities.class).application());
        startUpdate();
    }

    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    private void initView(final Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (!Settings.canDrawOverlays(context)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    context.startActivity(intent);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "open fps view fail", e);
        }

        LayoutInflater mLayoutInflater = LayoutInflater.from(context);
        mRootView = mLayoutInflater.inflate(R.layout.fps_layout, null);
        mFpsTv = mRootView.findViewById(R.id.fps_tv);
        mChart = mRootView.findViewById(R.id.to_chart_tv);
        mJankStack = mRootView.findViewById(R.id.to_jank_stack_tv);

        mAGradeDrawable = context.getResources().getDrawable(R.mipmap.fps_a_grade);
        mDGradeDrawable = context.getResources().getDrawable(R.mipmap.fps_d_grade);

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }

        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.gravity = Gravity.START;
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        mFpsTv.setOnClickListener(this);
        mFpsTv.setOnTouchListener(this);
        mChart.setOnClickListener(this);
        mChart.setOnTouchListener(this);
        mJankStack.setOnClickListener(this);
        mJankStack.setOnTouchListener(this);
        mWindowManager.addView(mRootView, mLayoutParams);

    }


    @Override
    public void onFrame(long frameTimeMillis, int frameCostMillis) {
        if (mState == STATE.UPDATE) {
            int skipped = (int) ((frameCostMillis * NANOS_PER_MS - FRAME_INTERVAL_NANOS) / FRAME_INTERVAL_NANOS);

            int fps = FPS_MAX_DEFAULT - skipped;
            if (fps < 0) {
                fps = 0;
            }
            mFpsTv.setText(fps + "");
            if (fps < FPS_LEVEL_BAD) {
                mFpsTv.setBackgroundDrawable(mDGradeDrawable);
            } else {
                mFpsTv.setBackgroundDrawable(mAGradeDrawable);
            }
            recordFrame(buildRecord(frameTimeMillis, frameCostMillis));

            mCurrentFrameIndex++;
        }
    }


    /**
     * 2^43 = 8796093022208 format data year-month-day is 2248-09-26
     * 2^19 = 524288 about 8 minute
     */
    @Override
    public long buildRecord(long frameTimeMillis, int frameCostMillis) {

        return (long) (frameCostMillis & 0x07FFFF) << 44 | frameTimeMillis;
    }

    @Override
    public long frameTimeMillis(long record) {
        return record & 0x0FFFFFFFFFFFL;
    }

    @Override
    public int frameCostMillis(long record) {
        return (int) (record >> 44 & 0x07FFFF);
    }

    @Override
    public void onRecord(boolean recording) {
        if (recording) {
            mRootView.setVisibility(View.VISIBLE);
        } else {
            mRootView.setVisibility(View.GONE);
        }
    }

    @Override
    public void startUpdate() {
        mCurrentFrameIndex = 0;
        mFpsTv.setVisibility(View.VISIBLE);
        mChart.setVisibility(View.GONE);
        mJankStack.setVisibility(View.GONE);
        mState = STATE.UPDATE;
    }

    private void stopUpdate() {
        mFpsTv.setVisibility(View.VISIBLE);
        mChart.setVisibility(View.VISIBLE);
        mJankStack.setVisibility(View.VISIBLE);
        mFpsTv.setText(R.string.go);
        mState = STATE.STOP;
    }


    @Override
    public void dismiss() {
        stopUpdate();
        mFpsTv.setVisibility(View.GONE);
        mChart.setVisibility(View.GONE);
        mJankStack.setVisibility(View.GONE);
    }


    private void recordFrame(long recordData) {
        if (mCurrentFrameIndex < FPS_MAX_COUNT_DEFAULT) {
            mFrameCostBuffer[mCurrentFrameIndex] = recordData;
        } else {
            mFrameCostBuffer[mCurrentFrameIndex % FPS_MAX_COUNT_DEFAULT] = recordData;
        }
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fps_tv) {
            if (mState == STATE.STOP ) {
                startUpdate();
            } else if (mState == STATE.UPDATE) {
                stopUpdate();
            }
        } else if (v.getId() == R.id.to_chart_tv ) {
            dismiss();
            TransferCenter.getImpl(INavigator.class).toFpsChatActivity(mFpsTv.getContext());
        } else if (v.getId() == R.id.to_jank_stack_tv) {
            dismiss();
            TransferCenter.getImpl(INavigator.class).toJankInfosActivity(mJankStack.getContext());
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartX = event.getRawX();
                mTouchStartY = event.getRawY();
                mStartPositionX = mLayoutParams.x;
                mStartPositionY = mLayoutParams.y;

                break;
            case MotionEvent.ACTION_MOVE:
                float rawX = event.getRawX();
                float rawY = event.getRawY();
                mLayoutParams.x += rawX - mTouchStartX;
                mLayoutParams.y += rawY - mTouchStartY;
                mTouchStartX = rawX;
                mTouchStartY = rawY;
                mWindowManager.updateViewLayout(mRootView, mLayoutParams);
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(mLayoutParams.x - mStartPositionX) < MOVE_ERROR && Math.abs(mLayoutParams.y
                        - mStartPositionY) < MOVE_ERROR) {

                    FpsLog.info("isFpsView:" + (v.getId() == R.id.fps_tv) + ";" + (v.getId() == R.id.to_chart_tv));
                    v.performClick();
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }

    @NotNull
    @Override
    public long[] recordDatas() {

        int frameLength = Math.min(mCurrentFrameIndex,FPS_MAX_COUNT_DEFAULT);

        long[] copyBuffer = new long[frameLength];

        int cycleIndex = mCurrentFrameIndex%FPS_MAX_COUNT_DEFAULT;

        if(mCurrentFrameIndex > FPS_MAX_COUNT_DEFAULT){
            System.arraycopy(mFrameCostBuffer, cycleIndex, copyBuffer, 0, FPS_MAX_COUNT_DEFAULT- cycleIndex);
            System.arraycopy(mFrameCostBuffer, 0, copyBuffer, FPS_MAX_COUNT_DEFAULT-cycleIndex,cycleIndex);
        }else {
            System.arraycopy(mFrameCostBuffer, 0, copyBuffer, 0, mCurrentFrameIndex);
        }

        return copyBuffer;
    }

    @Override
    public long periodStartTime() {
        FpsLog.info("periodStartTime "+mCurrentFrameIndex+",,"+mFrameCostBuffer[0]);
        if(mCurrentFrameIndex > FPS_MAX_COUNT_DEFAULT){
            return frameTimeMillis(mFrameCostBuffer[mCurrentFrameIndex%FPS_MAX_COUNT_DEFAULT]);
        }else {
            return frameTimeMillis(mFrameCostBuffer[0]);
        }

    }


    @Override
    public void buildDisplayStack(boolean push) {
        FpsLog.info("buildDisplayStack "+push);
        if(push) {
            stack++;
        }else {
            stack --;
        }
        if(stack >= 0) {
            stopUpdate();
        }else {
            dismiss();
        }
    }
}

