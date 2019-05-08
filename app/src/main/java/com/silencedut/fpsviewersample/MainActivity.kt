package com.silencedut.fpsviewersample

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread

import android.util.Log
import android.view.Choreographer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }
    private var mLastFrameNanos = 0L

    private var asyncHandler: Handler?=null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                testToastA(true)
                message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                testToastB()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    init {
        val asyncHandlerThread = HandlerThread("async")
        asyncHandlerThread.start()
        asyncHandler = Handler(asyncHandlerThread.looper)

    }

    fun testToastA(isFirst: Boolean) {
        var trueId = 0L
        trueId = trueId or (1L shl 63)
        Log.d(TAG, "exception on random${(trueId)}+${(trueId shr 63) and 0x1} , ${(1L shl 62)}")
        Thread.sleep(200)
        Log.d(TAG,"afterThread")
        if(isFirst) {
            testToastA(false)
        }

    }

    var start = 0L
    fun testToastB() {
        start = System.nanoTime()
        Choreographer.getInstance().postFrameCallback(callback)
        val delay = (1 until 3).random()
        Thread.sleep(delay*500L)
        testToastA(true)
        Log.d(TAG, "delay $delay ${start}")
    }


    override fun onStop() {
        super.onStop()

    }

    val callback = Choreographer.FrameCallback { frameTimeNanos ->
        //                if(mLastFrameNanos > 0) {
        //                    Log.d(TAG, "currentTimeNano $frameTimeNanos"+"diffLast ${frameTimeNanos - mLastFrameNanos}")
        //                }
        fpsView?.let {
            doCalculateFrame(frameTimeNanos,it)
        }

    }

    var fpsView :TextView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)


        fpsView = findViewById(R.id.message)


    }



    val mFrameIntervalNanos = 1000000000f / 60
    private fun doCalculateFrame(frameTimeNanos: Long,fpsView:TextView) {

        val diff = frameTimeNanos - mLastFrameNanos


        val now = System.nanoTime()

        val differ = now - frameTimeNanos



        val skipped = (diff - mFrameIntervalNanos)/ mFrameIntervalNanos

        Log.d(TAG, "current $now"+"frameTimeNanos $frameTimeNanos differ $differ")
        Log.d(TAG, "fps ${60-skipped}"+"diff $diff differ${(frameTimeNanos -start)/mFrameIntervalNanos} ")

        fpsView.text = (60-skipped).toString()

        mLastFrameNanos = frameTimeNanos
//        val delay = (1 until 3).random()
//        Thread.sleep(delay*1000L)
//        Choreographer.getInstance().postFrameCallback(callback)

    }


    private fun IntRange.random() : Int {
        try {
            return Random().nextInt((endInclusive + 1) - start) +  start
        }catch (e : Exception) {
            Log.d(TAG,"exception on random",e)
        }
        return 0
    }

}
