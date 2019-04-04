package com.silencedut.fpsviewer.background


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