package com.github.xiaofei_dev.vibrator.util

import android.content.Context
import android.widget.Toast

/**
 * author：xiaofei_dev
 * time：2017/5/15:8:47
 * e-mail：xiaofei.dev@gmail.com
 * desc：coding
 */
object ToastUtil {
    private var toast: Toast? = null

    fun showToast(context: Context,
                  content: String) {
        if (toast == null) {
            toast = Toast.makeText(context,
                    content,
                    Toast.LENGTH_SHORT)
        } else {
            toast!!.setText(content)
        }
        toast!!.show()
    }
}
