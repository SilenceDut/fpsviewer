package com.silencedut.fpsviewer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View

/**
 * @author SilenceDut
 * @date 2019/5/6
 */
abstract class BaseFpsViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(provideContentViewId())
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        } else {
            findViewById<View>(R.id.finish_layout).visibility = View.VISIBLE
            findViewById<View>(R.id.finish).setOnClickListener { finish() }
        }
        initViews()
    }

    abstract fun provideContentViewId():Int

    abstract fun initViews()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                this.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}