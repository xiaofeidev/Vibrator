package com.github.xiaofei_dev.vibrator.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.xiaofei_dev.vibrator.App
import com.github.xiaofei_dev.vibrator.R
import com.github.xiaofei_dev.vibrator.extension.setTheme
import com.github.xiaofei_dev.vibrator.singleton.Preference
import com.github.xiaofei_dev.vibrator.singleton.PurchaseStatus
import com.github.xiaofei_dev.vibrator.util.OpenUtil
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.AdapterStatus
// 取消 Kotlin synthetic 导入，改用 findViewById
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.ads.AdView
//import org.jetbrains.anko.find

class AboutActivity : AppCompatActivity(),
        View.OnClickListener {

    // 使用 lateinit 保存视图引用，替代 Kotlin synthetic
    private lateinit var toolbar: Toolbar
    private lateinit var adView: AdView
    private lateinit var textVersion: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_about)

        // 通过 findViewById 初始化视图
        toolbar = findViewById(R.id.toolbar)
        adView = findViewById(R.id.adView)
        textVersion = findViewById(R.id.textVersion)

        initAd()
        setSupportActionBar(toolbar)
        //toolbar.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //find<View>(R.id.itemOpenSource).setOnClickListener(this)
        findViewById<View>(R.id.itemScoreAndFeedback).setOnClickListener(this)
        //find<View>(R.id.itemDonate).setOnClickListener(this)
        textVersion.text = getString(R.string.app_version, getPackageVersion(this))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finishAfterTransition()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View) {
        when (view.id) {
            /*R.id.itemOpenSource -> {
                val url = getString(R.string.openSourceLink)
                OpenUtil.openLink(view.context, null, url, false)
            }*/
            R.id.itemScoreAndFeedback -> OpenUtil.openApplicationMarket(packageName, "com.android.vending",
                    view.context)
            /*R.id.itemDonate ->
                OpenUtil.alipayDonate(this)*/
        }
    }

    //加载广告
    private fun initAd(){
        if (Preference.mPurchaseStatus == PurchaseStatus.BOUGHT){
            return
        }
        //初始化 AdMob
        //MobileAds.getInitializationStatus()?.adapterStatusMap?.get(MobileAds::class.qualifiedName)
        if (App.adState != AdapterStatus.State.READY){
            MobileAds.initialize(this) {
                it.adapterStatusMap?.get(MobileAds::class.qualifiedName)?.initializationState?.let {
                    App.adState = it
                    val adRequest = AdRequest.Builder().build()
                    adView.loadAd(adRequest)
                }
            }
        } else {
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }
    }

    /**
     * 获取当前App的版本
     * @param context
     * @return
     */
    private fun getPackageVersion(context: Context): String? {
        val manager = context.packageManager
        var name: String? = null
        try {
            val info = manager.getPackageInfo(context.packageName, 0)
            name = info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return name
    }
}