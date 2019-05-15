package com.silencedut.fpsviewer.datashow

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.silencedut.fpsviewer.R

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

    abstract fun provideContentViewId():Int

    abstract fun initViews()

}