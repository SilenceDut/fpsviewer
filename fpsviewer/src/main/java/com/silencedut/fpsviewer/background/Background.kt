package com.duowan.makefriends.framework.context

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.os.Bundle
import com.silencedut.fpsviewer.FpsLog
import java.util.HashSet


/**
 * 监听app生命周期,处理前后台事件
 */
object Background {

    private val callBacks = HashSet<BackgroundCallback>()
    private var mActivityStopTimes = 0
    fun registerBackgroundCallback(backgroundCallback: BackgroundCallback){
        callBacks.add(backgroundCallback)
    }

    fun init(application: Application) {
        val activityCallbacks = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityStarted(activity: Activity?) {


                if (activity != null) {
                    mActivityStopTimes --
                    if (mActivityStopTimes == -1) {
                        callBacks.forEach {
                            it.onBack2foreground()
                        }
                    }
                }
            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity?) {

                if (activity != null) {
                    mActivityStopTimes ++
                    if(mActivityStopTimes == 0) {
                       callBacks.forEach {
                           it.onFore2background()
                       }
                   }
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }
        }
        application.registerActivityLifecycleCallbacks(activityCallbacks)
    }

    /**
     * 判断应用是否后台运行
     */
    private fun isBackgroundRunning(activity: Activity?): Boolean {
        try {
            val processName = activity?.packageName
            val activityManager = activity?.getSystemService(Activity.ACTIVITY_SERVICE) as? ActivityManager
                ?: return false

            val processList = activityManager.runningAppProcesses ?: return false
            for (process in processList) {
                if (process.processName.equals(processName, ignoreCase = true)) {
                    return process.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                        process.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
                }
            }
        } catch (t: Throwable) {
            FpsLog.error("isBackgroundRunning error$t")
        }

        return false
    }
}
