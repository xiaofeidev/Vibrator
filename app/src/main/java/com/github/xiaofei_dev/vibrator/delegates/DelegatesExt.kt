package com.github.xiaofei_dev.vibrator.delegates

import android.content.Context

/**
 * Created by xiaofei on 2017/10/13.
 */
object DelegatesExt {
    fun <T : Any> preference(context: Context, name: String, default: T)
            = Preference(context, name, default)
}