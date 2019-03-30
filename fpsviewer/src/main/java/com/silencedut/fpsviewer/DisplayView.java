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

    private Context mApplicationContext;
    private WindowManager windowManager ;
    private WindowManager.LayoutParams layoutParams;

    private float startPositionX = 0;
    private float startPositionY = 0;

    private float mTouchStartX;
    private float mTouchStartY;
    private boolean started = false;
    private Drawable mDGradeDrawable;
    private Drawable mAGradeDrawable;

    /**
     * default about 10 minute(60*10)
     */
    private int[] fpsBuffer = new int[36000];
    private int totalBufferIndex;
    private volatile boolean inSample;
    private int sampleCount;

    public static DisplayView show(final Context context ) {
        return new DisplayView(context);
    }

    @SuppressLint("InflateParams")
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

        mApplicationContext = context;
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

        mRootView.setOnClickListener(this);
        mRootView.setOnTouchListener(this);

        FpsViewer.getFpsMonitor().addFrameListener(new FpsMonitor.FrameListener() {
            @Override
            public void onFrame(byte fps, int skipped, long frameCostMillis) {
                if(started) {
                    mFpsTv.setText(context.getString(R.string.fps,fps));
                    if(fps < FPS_D) {
                        mFpsTv.setBackground(mDGradeDrawable);
                    } else {
                        mFpsTv.setBackground(mAGradeDrawable);
                    }
                    recordFrame(fps,skipped);
                }
            }
        });
        windowManager.addView(mRootView, layoutParams);
    }

    public void syncSample() {
        if( inSample ) {
            return;
        }
        inSample = true;
        sampleCount = 0;
    }

    private void waitForNextSample() {
        Sentry.sentryScheduler().sendMessage(SentryScheduler.SAMPLE_PERIOD,Sentry.sentryConfig().fpsSamplePeriod());
    }


    private void recordFrame(byte fps,int skipped) {

        if(inSample) {

            int combine = fps << 26 ;
            combine = skipped | combine;
            if(totalBufferIndex <= fpsBuffer.length) {
                fpsBuffer[totalBufferIndex] = combine;
            } else {
                toggle();
            }
            totalBufferIndex++;
            if(sampleCount < Sentry.sentryConfig().fpsSampleFrameCount()-1) {
                sampleCount ++ ;
            }else {
                inSample = false;
                waitForNextSample();
            }
        }

    }

    @Override
    public void onClick(View v) {
        toggle();
    }

    private void toggle() {
        started = !started;
        if(!started) {
            mFpsTv.setText(R.string.go);
            Intent intent = new Intent(mApplicationContext,FpsAnalyzeActivity.class);

            int[] copyBuffer  = new int[totalBufferIndex];
            System.arraycopy(fpsBuffer,0,copyBuffer,0,copyBuffer.length);
            Log.i(TAG,"buffer length "+copyBuffer.length);
            intent.putExtra(FpsAnalyzeActivity.FPS_BUFFER,copyBuffer);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mApplicationContext.startActivity(intent);
        } else {
            totalBufferIndex = 0;
            waitForNextSample();
        }

        mAnalyze.setVisibility(started?View.GONE:View.VISIBLE);

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
                    v.performClick();

                    return true;
                }
                break;
            default: break;
        }
        return false;
    }
}
