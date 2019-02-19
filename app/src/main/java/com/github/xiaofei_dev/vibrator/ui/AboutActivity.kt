package com.github.xiaofei_dev.vibrator.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.github.xiaofei_dev.vibrator.R
import com.github.xiaofei_dev.vibrator.util.OpenUtil
import org.jetbrains.anko.find

class AboutActivity : AppCompatActivity(),
        View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        find<View>(R.id.itemOpenSource).setOnClickListener(this)
        find<View>(R.id.itemScoreAndFeedback).setOnClickListener(this)
        find<View>(R.id.itemDonate).setOnClickListener(this)
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
}