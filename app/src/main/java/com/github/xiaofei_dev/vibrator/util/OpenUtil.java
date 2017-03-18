package com.github.xiaofei_dev.vibrator.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class OpenUtil {
    public static void openApplicationMarket(String appPackageName, String marketPackageName,
                                             Context context) {
        try {
            String url = "market://details?id=" + appPackageName;
            Intent localIntent = new Intent(Intent.ACTION_VIEW);

            if (marketPackageName != null) {
                localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                localIntent.setPackage(marketPackageName);
            }
            openLink(context, localIntent, url, true);
        } catch (Exception e) {
            e.printStackTrace();
            openApplicationMarketForLinkBySystem(appPackageName, context);
        }
    }

    public static void openApplicationMarketForLinkBySystem(String packageName, Context context) {
        String url = "http://www.coolapk.com/apk/" + packageName;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        openLink(context, intent, url, false);
    }

    public static void openLink(Context context, Intent intent, String link, boolean isThrowException) {
        if (intent == null) {
            intent = new Intent(Intent.ACTION_VIEW);
        }

        try {
            intent.setData(Uri.parse(link));
            context.startActivity(intent);
        } catch (Exception e) {
            if (isThrowException) {
                throw e;
            } else {
                e.printStackTrace();
                Toast.makeText(context, "打开失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
