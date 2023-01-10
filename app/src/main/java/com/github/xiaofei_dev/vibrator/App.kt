package com.github.xiaofei_dev.vibrator
import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.AdapterStatus


/**
 * 作者：XiaoFei
 * 日期：2017/7/10.
 */
class App : Application() {
    companion object {
        lateinit var instance: App
        var adState = AdapterStatus.State.NOT_READY
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        MobileAds.initialize(this) {
            it.adapterStatusMap.get(MobileAds::class.qualifiedName)?.initializationState?.let {
                adState = it
            }
        }
    }
}
