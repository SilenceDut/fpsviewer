package com.silencedut.fpsviewer.utilities

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.silencedut.fpsviewer.api.IUtilities
import com.silencedut.hub_annotation.HubInject
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author SilenceDut
 * @date 2019-05-14
 */
@HubInject(api = [IUtilities::class])
class Utilities : IUtilities {
    private val mMainHandler = Handler(Looper.getMainLooper())
    lateinit var appContext: Application
    override fun onCreate() {
    }

    override fun traceToString(skipStackCount: Int, stackArray: Array<Any>): String {

        if (stackArray.isEmpty()) {
            return "[]"
        }

        val b = StringBuilder()
        for (i in 0 until stackArray.size - skipStackCount) {
            if (i == stackArray.size - skipStackCount - 1) {
                return b.toString()
            }
            b.append(stackArray[i])
            b.append("\n")
        }
        return b.toString()
    }

    override fun ms2Date(ms: Long): String {
        val date = Date(ms)
        val format = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
        return format.format(date)
    }

    override fun mainHandler():Handler {
        return mMainHandler
    }

    override fun application() :Application{
        return appContext
    }

    override fun setApplication(application: Application) {
        this.appContext = application
    }


}