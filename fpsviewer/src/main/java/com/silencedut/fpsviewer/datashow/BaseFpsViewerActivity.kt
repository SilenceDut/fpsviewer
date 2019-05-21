package com.silencedut.fpsviewer.datashow

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

import com.silencedut.fpsviewer.R
import com.silencedut.fpsviewer.api.IDisplayFps

import com.silencedut.fpsviewer.transfer.TransferCenter
import com.silencedut.fpsviewer.utilities.FpsLog

/**
 * @author SilenceDut
 * @date 2019/5/6
 */
abstract class BaseFpsViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(provideContentViewId())
        supportActionBar?.hide()
        findViewById<View>(R.id.finish_layout).visibility = View.VISIBLE
        findViewById<View>(R.id.finish).setOnClickListener { finish() }
        initViews()
    }

    override fun onResume() {
        super.onResume()
        FpsLog.info("onResume:$this")
    }

    override fun onStart() {
        super.onStart()
        FpsLog.info("onStart:$this")
        TransferCenter.getImpl(IDisplayFps::class.java).buildDisplayStack(false)
    }

    override fun onRestart() {
        super.onRestart()
        FpsLog.info("onRestart:$this")

    }

    override fun onStop() {
        super.onStop()
        FpsLog.info("onStart:$this")
        TransferCenter.getImpl(IDisplayFps::class.java).buildDisplayStack(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        FpsLog.info("onDestroy:$this")

    }

    abstract fun provideContentViewId():Int

    abstract fun initViews()

}