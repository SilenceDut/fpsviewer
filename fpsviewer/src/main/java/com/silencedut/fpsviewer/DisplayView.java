package com.silencedut.fpsviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import com.silencedut.fpsviewer.analyze.FpsAnalyzeActivity;

import static com.silencedut.fpsviewer.FpsConstants.FPS_MAX_COUNT_DEFAULT;


/**
 * @author SilenceDut
 * @date 2019/3/26
 */
public class DisplayView implements View.OnClickListener ,View.OnTouchListener, FpsEventRelay.FrameListener {


    enum STATE {
        /**
         * 浮窗View状态
         */
        INITIAL, UPDATE,STOP,ANALYZE
    }
    private static final String TAG = "FpsViewer";
    private static final int MOVE_ERROR = 20;
    private static final int FPS_LEVEL_BAD = 15;

    private View mRootView;
    private TextView mFpsTv;
    private TextView mAnalyze;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;

    private float mStartPositionX = 0;
    private float mStartPositionY = 0;

    private float mTouchStartX;
    private float mTouchStartY;

    private Drawable mDGradeDrawable;
    private Drawable mAGradeDrawable;

    private STATE mState = STATE.INITIAL;

    private int[] mFrameCostBuffer = new int[FPS_MAX_COUNT_DEFAULT];

    private int mStartFrameIndex;
    private int mCurrentFrameIndex;

    private long mStartTime;

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
        mAnalyze.setOnClickListener(this);
        mAnalyze.setOnTouchListener(this);
        mWindowManager.addView(mRootView, mLayoutParams);

    }


    @Override
    public void onFrame(int frameIndex, int skipped, int frameCostMillis, byte fps) {
        if(mState == STATE.UPDATE) {
            mFpsTv.setText(fps+"");
            if(fps < FPS_LEVEL_BAD) {
                mFpsTv.setBackgroundDrawable(mDGradeDrawable);
            } else {
                mFpsTv.setBackgroundDrawable(mAGradeDrawable);
            }
            recordFrame(frameIndex,frameCostMillis);
            mCurrentFrameIndex = frameIndex;
        }
    }

    @Override
    public void onRecord(boolean recording) {
        if(recording) {
            mRootView.setVisibility(View.VISIBLE);
        }else {
            mRootView.setVisibility(View.GONE);
        }
    }



    DisplayView prepare() {
        FpsViewer.fpsEventRelay().addFrameListener(this);
        initial();
        return this;
    }

    private void startUpdate() {
        mStartFrameIndex = FpsViewer.fpsEventRelay().currentFrameIndex();
        mStartTime = SystemClock.elapsedRealtime();
        mFpsTv.setVisibility(View.VISIBLE);
        mAnalyze.setVisibility(View.GONE);

    }

    private void stopUpdate() {
        mAnalyze.setVisibility(View.VISIBLE);
        mFpsTv.setText(R.string.go);
        long duration = SystemClock.elapsedRealtime() - mStartTime;
        FpsLog.info("duration:"+duration);
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

    /**
     * if frameIndex > FPS_MAX_COUNT_DEFAULT ,just crash
     */
    private void recordFrame(int frameIndex ,int frameCost) {
        try {
            mFrameCostBuffer[frameIndex] = frameCost;
        }catch (Exception e) {
            FpsLog.error("recordFrame error ,"+e);
        }

    }


    @Override
    public void onClick(View v) {

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
        }else if(v.getId() == R.id.analyze && mCurrentFrameIndex < FPS_MAX_COUNT_DEFAULT && mStartFrameIndex < FPS_MAX_COUNT_DEFAULT) {
            mState = STATE.ANALYZE;
            dismiss();
            Intent intent = new Intent(mFpsTv.getContext(),FpsAnalyzeActivity.class);

            int frameLength = mCurrentFrameIndex - mStartFrameIndex ;

            int[] copyBuffer  = new int[frameLength];
            System.arraycopy(mFrameCostBuffer,mStartFrameIndex,copyBuffer,0, frameLength);

            Log.i(TAG,"buffer length "+copyBuffer.length);
            intent.putExtra(FpsAnalyzeActivity.FPS_BUFFER,copyBuffer);
            intent.putExtra(FpsAnalyzeActivity.FPS_BUFFER_START,mStartFrameIndex);
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
                mStartPositionX = mLayoutParams.x;
                mStartPositionY = mLayoutParams.y;

                break;
            case MotionEvent.ACTION_MOVE:
                float rawX = event.getRawX();
                float rawY = event.getRawY();
                mLayoutParams.x +=  rawX - mTouchStartX;
                mLayoutParams.y +=  rawY - mTouchStartY;
                mTouchStartX = rawX;
                mTouchStartY = rawY;
                mWindowManager.updateViewLayout(mRootView, mLayoutParams);
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(mLayoutParams.x - mStartPositionX) < MOVE_ERROR && Math.abs(mLayoutParams.y
                        - mStartPositionY) < MOVE_ERROR) {

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

