package com.silencedut.fpsviewersample

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.design.widget.BottomNavigationView
import android.support.v4.os.HandlerCompat
import android.support.v7.app.AppCompatActivity

import android.util.Log
import android.view.Choreographer
import android.widget.TextView
import com.silencedut.fpsviewer.FpsViewer


import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.locks.AbstractQueuedSynchronizer
import android.view.Display



class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private var mLastFrameNanos = 0L

    private var asyncHandler: Handler? = null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                testToastA(true)
                message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                FpsViewer.getViewer().appendSection("TestSection")
                testToastB()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                FpsViewer.getViewer().removeSection("TestSection")
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
        Log.d(TAG, "afterThread")
        if (isFirst) {
            // testToastA(false)
        }
        GlobalScope.launch(Dispatchers.Main) {
            Log.d(TAG, "GlobalScope before ${Thread.currentThread()}")

            Log.d(TAG, "GlobalScope testIo  ${Thread.currentThread()}")


//            withContext(Dispatchers.IO) {
//                suspendCancellableCoroutine<String> { cancellableContinuation ->
//                    cancellableContinuation.resume("hah")
//                }
//                Log.d(TAG,"GlobalScope withContext ${Thread.currentThread()}")
//                //  Thread.sleep(5000)
//                //  Log.d(TAG,"GlobalScope withContext thread after ${Thread.currentThread()}")
//                delay(5000)
//                Log.d(TAG,"GlobalScope withContext delay after ${Thread.currentThread()}")
//            }
//            val res1 =async(Dispatchers.Default){
//
//                Log.d(TAG,"GlobalScope withContext ${Thread.currentThread()}")
//                //  Thread.sleep(5000)
//                //  Log.d(TAG,"GlobalScope withContext thread after ${Thread.currentThread()}")
//                delay(5000)
//                Log.d(TAG,"GlobalScope withContext delay after ${Thread.currentThread()}")
//            }
//
//            val res2 =async(Dispatchers.Default){
//
//                Log.d(TAG,"GlobalScope withContext ${Thread.currentThread()}")
//                //  Thread.sleep(5000)
//                //  Log.d(TAG,"GlobalScope withContext thread after ${Thread.currentThread()}")
//                delay(5000)
//                Log.d(TAG,"GlobalScope withContext delay after ${Thread.currentThread()}")
//            }
//            res1.await()
//            res2.await()
            val res = testIo2()
            Log.d(TAG, "GlobalScope res  $res ,${Thread.currentThread()}")


            // Log.d(TAG,"GlobalScope delay $res after ${Thread.currentThread()}")
            //val res2 = testIo2()

        }

        Log.d(TAG, "GlobalScope after  ${Thread.currentThread()}")
        Thread.sleep(10)
        Log.d(TAG, "GlobalScope after sleep ${Thread.currentThread()}")
    }

    suspend fun testIo2() = suspendCancellableCoroutine<String> { continuation ->
        Thread {
            Log.d(TAG, "GlobalScope testIo2 ${Thread.currentThread()}")
            Thread.sleep(5000)
            continuation.resume("HaHa")
        }.start()

    }

    suspend fun test() = suspendCancellableCoroutine<String> { cancellableContinuation ->
        cancellableContinuation.resume("hah")
    }


    suspend fun testIo(): String {
        Log.d(TAG, "GlobalScope testIo  ${Thread.currentThread()}")

        withContext(Dispatchers.IO) {
            Log.d(TAG, "GlobalScope withContext ${Thread.currentThread()}")
            //  Thread.sleep(5000)
            //  Log.d(TAG,"GlobalScope withContext thread after ${Thread.currentThread()}")
            delay(5000)
            Log.d(TAG, "GlobalScope withContext delay after ${Thread.currentThread()}")
        }
        Log.d(TAG, "GlobalScope res  ${Thread.currentThread()}")
        return "aaa"
    }

    var start = 0L
    fun testToastB() {
        start = System.nanoTime()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Choreographer.getInstance().postFrameCallback(callback)
        }
        val delay = (3 until 5).random()
        Thread.sleep(delay * 500L)
        testToastA(true)
        Log.d(TAG, "delay  ${start}")
    }


    override fun onStop() {
        super.onStop()

    }

    val callback = Choreographer.FrameCallback { frameTimeNanos ->
        //                if(mLastFrameNanos > 0) {
        //                    Log.d(TAG, "currentTimeNano $frameTimeNanos"+"diffLast ${frameTimeNanos - mLastFrameNanos}")
        //                }
        fpsView?.let {
            doCalculateFrame(frameTimeNanos, it)
        }

    }

    var fpsView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)


        fpsView = findViewById(R.id.message)
        val display = windowManager.defaultDisplay
        val refreshRate = display.refreshRate
        Log.d(TAG, "refreshRate  ${refreshRate}")
    }


    val mFrameIntervalNanos = 1000000000f / 60
    var lastCurrent =0L
    private fun doCalculateFrame(frameTimeNanos: Long, fpsView: TextView) {

        val diff = frameTimeNanos - mLastFrameNanos


        val now = System.nanoTime()

        val differ = now - frameTimeNanos


        val skipped = (diff - mFrameIntervalNanos) / mFrameIntervalNanos
//
//        Log.d(TAG, "current $now" + "frameTimeNanos $frameTimeNanos differ ${now -lastCurrent}")
//        Log.d(TAG, "fps ${60 - skipped}" + "diff ${ frameTimeNanos - mLastFrameNanos}  ")

        fpsView.text = (60 - skipped).toString()

        mLastFrameNanos = frameTimeNanos
        lastCurrent = now
//        val delay = (1 until 3).random()
//        Thread.sleep(delay*1000L)
        Choreographer.getInstance().postFrameCallback(callback)

    }


    private fun IntRange.random(): Int {
        try {
            return Random().nextInt((endInclusive + 1) - start) + start
        } catch (e: Exception) {
            Log.d(TAG, "exception on random", e)
        }
        return 0
    }

}
