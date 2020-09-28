package com.github.xiaofei_dev.vibrator.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.xiaofei_dev.vibrator.R
import com.github.xiaofei_dev.vibrator.util.OpenUtil
import kotlinx.android.synthetic.main.activity_about.*
import org.jetbrains.anko.find

class AboutActivity : AppCompatActivity(),
        View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        find<View>(R.id.itemOpenSource).setOnClickListener(this)
        find<View>(R.id.itemScoreAndFeedback).setOnClickListener(this)
        find<View>(R.id.itemDonate).setOnClickListener(this)
        textVersion.setText(getString(R.string.app_version, getPackageVersion(this)))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finishAfterTransition()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.itemOpenSource -> {
                val url = getString(R.string.openSourceLink)
                OpenUtil.openLink(view.context, null, url, false)
            }
            R.id.itemScoreAndFeedback -> OpenUtil.openApplicationMarket(packageName, "com.coolapk.market",
                    view.context)
            R.id.itemDonate ->
                OpenUtil.alipayDonate(this)
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