package com.github.xiaofei_dev.vibrator.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object OpenUtil {
    fun openApplicationMarket(appPackageName: String, marketPackageName: String?,
                              context: Context) {
        try {
            val url = "market://details?id=" + appPackageName
            val localIntent = Intent(Intent.ACTION_VIEW)

            if (marketPackageName != null) {
                localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                localIntent.`package` = marketPackageName
            }
            openLink(context, localIntent, url, true)
        } catch (e: Exception) {
            e.printStackTrace()
            openApplicationMarketForLinkBySystem(appPackageName, context)
        }

    }

    fun openApplicationMarketForLinkBySystem(packageName: String, context: Context) {
        val url = "http://www.coolapk.com/apk/" + packageName
        val intent = Intent(Intent.ACTION_VIEW)
        openLink(context, intent, url, false)
    }

    fun openLink(context: Context, intent: Intent?, link: String, isThrowException: Boolean) {
        var intent = intent
        if (intent == null) {
            intent = Intent(Intent.ACTION_VIEW)
        }

        try {
            intent.data = Uri.parse(link)
            context.startActivity(intent)
        } catch (e: Exception) {
            if (isThrowException) {
                throw e
            } else {
                e.printStackTrace()
                Toast.makeText(context, "打开失败", Toast.LENGTH_SHORT).show()
            }
        }

    }

    //打开自己的支付宝付款链接
    fun alipayDonate(context: Context) {
        val intent = Intent()
        intent.action = "android.intent.action.VIEW"
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        val payUrl = "https://qr.alipay.com/FKX06496G2PCRYR0LXR7BC"
        intent.data = Uri.parse("alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=" + payUrl)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            intent.data = Uri.parse(payUrl)
            context.startActivity(intent)
        }
    }
}
