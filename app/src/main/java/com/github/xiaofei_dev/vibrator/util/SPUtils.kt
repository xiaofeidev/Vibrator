package com.xiaofeidev.delegatedemo.delegates

import android.content.Context
import com.github.xiaofei_dev.vibrator.App

/**
 * @author xiaofei_dev
 * @desc 读写 SP 存储项的基础工具类
 */

object SPUtils {
    val SP by lazy {
        App.instance.getSharedPreferences("default", Context.MODE_PRIVATE)
    }

    //读 SP 存储项
    fun <T> getValue(name: String, default: T): T = with(SP) {
        val res: Any = when (default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default) ?: ""
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> throw java.lang.IllegalArgumentException()
        }
        @Suppress("UNCHECKED_CAST")
        res as T
    }

    //写 SP 存储项
    fun <T> putValue(name: String, value: T) = with(SP.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> throw IllegalArgumentException("This type can't be saved into Preferences")
        }.apply()
    }
}