package com.duowan.makefriends.framework.context

/**
 * 应用后台状态通知
 * Created by zhongyongsheng on 2018/5/2.
 */
interface BackgroundCallback {
    /**
     * 从后台切到前台
     */
    fun onBack2foreground()

    /**
     * 从前台切到后台
     */
    fun onFore2background()
}