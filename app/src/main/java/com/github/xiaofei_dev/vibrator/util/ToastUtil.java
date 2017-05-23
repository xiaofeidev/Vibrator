package com.github.xiaofei_dev.vibrator.util;

import android.content.Context;
import android.widget.Toast;

/**
 * author：xiaofei_dev
 * time：2017/5/15:8:47
 * e-mail：xiaofei.dev@gmail.com
 * desc：coding
 */
public final class ToastUtil {

    private static Toast toast;

    public static void showToast(Context context,
                                 String content) {
        if (toast == null) {
            toast = Toast.makeText(context,
                    content,
                    Toast.LENGTH_SHORT);
        } else {
            toast.setText(content);
        }
        toast.show();
    }
}
