package com.github.xiaofei_dev.vibrator
import android.app.Application



/**
 * 作者：XiaoFei
 * 日期：2017/7/10.
 */
class App : Application() {
    companion object {
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
