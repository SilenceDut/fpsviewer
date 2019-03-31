package com.silencedut.fpsviewer;

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
import com.silencedut.fpsviewer.analyze.FpsAnalyzeActivity;

/**
 * @author SilenceDut
 * @date 2019/3/26
 */
public class DisplayView implements View.OnClickListener ,View.OnTouchListener{

    enum STATE {
        /**
         * 浮窗View状态
         */
        INITIAL, UPDATE,STOP,ANALYZE
    }
    private static final String TAG = "FpsViewer";
    private static final int MOVE_ERROR = 20;
    private static final int FPS_A = 50;
    private static final int FPS_B= 45;
    private static final int FPS_C= 30;
    private static final int FPS_D= 15;
    private static final int FPS_ZERO= 0;

    private View mRootView;
    private TextView mFpsTv;
    private TextView mAnalyze;

    private WindowManager windowManager ;
    private WindowManager.LayoutParams layoutParams;

    private float startPositionX = 0;
    private float startPositionY = 0;

    private float mTouchStartX;
    private float mTouchStartY;

    private Drawable mDGradeDrawable;
    private Drawable mAGradeDrawable;

    private STATE mState = STATE.INITIAL;
    private boolean mBufferFull;

    /**
     * default about 10 minute(60*10)
     */
    private int[] mFpsBuffer = new int[36000];
    private int mTotalBufferIndex;


    static DisplayView create(final Context context ) {
        return new DisplayView(context);
    }

    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    private DisplayView(final Context context) {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if(!Settings.canDrawOverlays(context)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    context.startActivity(intent);
                }
            }
        } catch (Exception e) {
            Log.e(TAG,"open fps view fail",e);
        }

        LayoutInflater mLayoutInflater = LayoutInflater.from(context);
        mRootView = mLayoutInflater.inflate(R.layout.fps_layout, null);
        mFpsTv  = mRootView.findViewById(R.id.fps_tv);
        mAnalyze = mRootView.findViewById(R.id.analyze);

        mAGradeDrawable =  context.getResources().getDrawable(R.drawable.fps_a_bg);
        mDGradeDrawable = context.getResources().getDrawable(R.drawable.fps_d_bg);

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }

        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.gravity = Gravity.START;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        mFpsTv.setOnClickListener(this);
        mFpsTv.setOnTouchListener(this);
        mAnalyze.setOnClickListener(this);
        mAnalyze.setOnTouchListener(this);


    }

    DisplayView prepare() {
        FpsViewer.fpsMonitor().addFrameListener(new FpsMonitor.FrameListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onFrame(byte fps, int skipped, long frameCostMillis) {
                if(mState == STATE.UPDATE) {
                    mFpsTv.setText(fps+"");
                    if(fps < FPS_D) {
                        mFpsTv.setBackground(mDGradeDrawable);
                    } else {
                        mFpsTv.setBackground(mAGradeDrawable);
                    }
                    recordFrame(fps,skipped);
                }
            }

            @Override
            public void onRecord(boolean recording) {
                if(recording) {
                    windowManager.addView(mRootView, layoutParams);
                }else {
                    windowManager.removeView(mRootView);
                }
            }
        });
        initial();
        return this;
    }

    private void startUpdate() {
        mTotalBufferIndex = 0;
        mBufferFull = false;
        mFpsTv.setVisibility(View.VISIBLE);
        mAnalyze.setVisibility(View.GONE);

    }

    private void stopUpdate() {
        mAnalyze.setVisibility(View.VISIBLE);
        mFpsTv.setText(R.string.go);
    }

    private void dismiss() {
        mFpsTv.setVisibility(View.GONE);
        mAnalyze.setVisibility(View.GONE);
    }

    public void initial() {
        mState = STATE.INITIAL;
        mFpsTv.setVisibility(View.VISIBLE);
        mAnalyze.setVisibility(View.GONE);
        mFpsTv.setText(R.string.go);
    }



    private void recordFrame(byte fps,int skipped) {

        int combine = fps << 26 ;
        combine = skipped | combine;
        if(mTotalBufferIndex < mFpsBuffer.length) {
            mFpsBuffer[mTotalBufferIndex] = combine;
        } else {
            mTotalBufferIndex = -1;
            mBufferFull = true;
        }
        mTotalBufferIndex++;


    }

    @Override
    public void onClick(View v) {
        FpsLog.info("onClick:"+(v.getId() == R.id.fps_tv)+";"+(v.getId() == R.id.analyze));

        if(v.getId() == R.id.fps_tv) {
            if(mState.ordinal() < STATE.ANALYZE.ordinal()) {

                if(mState == STATE.STOP || mState == STATE.INITIAL) {
                    startUpdate();
                    mState = STATE.UPDATE;
                }else if(mState == STATE.UPDATE) {
                    stopUpdate();
                    mState = STATE.STOP;
                }
            }
        }else if(v.getId() == R.id.analyze) {
            mState = STATE.ANALYZE;
            dismiss();
            Intent intent = new Intent(mFpsTv.getContext(),FpsAnalyzeActivity.class);

            int[] copyBuffer  = new int[mTotalBufferIndex];
            System.arraycopy(mFpsBuffer,0,copyBuffer,0,copyBuffer.length);
            Log.i(TAG,"buffer length "+copyBuffer.length);
            intent.putExtra(FpsAnalyzeActivity.FPS_BUFFER,copyBuffer);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mFpsTv.getContext().startActivity(intent);
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {


        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartX = event.getRawX();
                mTouchStartY = event.getRawY();
                startPositionX = layoutParams.x;
                startPositionY = layoutParams.y;

                break;
            case MotionEvent.ACTION_MOVE:
                float rawX = event.getRawX();
                float rawY = event.getRawY();
                layoutParams.x +=  rawX - mTouchStartX;
                layoutParams.y +=  rawY - mTouchStartY;
                mTouchStartX = rawX;
                mTouchStartY = rawY;
                windowManager.updateViewLayout(mRootView, layoutParams);
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(layoutParams.x - startPositionX) < MOVE_ERROR && Math.abs(layoutParams.y
                        - startPositionY) < MOVE_ERROR) {

                    FpsLog.info("isFpsView:"+(v.getId() == R.id.fps_tv)+";"+(v.getId() == R.id.analyze));
                    v.performClick();
                    return true;
                }
                break;
            default: break;
        }
        return false;
    }

}

