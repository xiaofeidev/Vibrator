package com.github.xiaofei_dev.vibrator
import android.app.Application
import android.content.Context



/**
 * 作者：XiaoFei
 * 日期：2017/7/10.
 */
class App : Application() {


    companion object {
        lateinit var instance: App
//        var appConfig: AppConfig? = null
    }

//    fun getAppConfig(): AppConfig {
//        return appConfig!!
//    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }



    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }
}
