package com.github.xiaofei_dev.vibrator.singleton

import com.github.xiaofei_dev.vibrator.App
import com.github.xiaofei_dev.vibrator.delegates.DelegatesExt
import com.github.xiaofei_dev.vibrator.util.VibratorUtil

/**
 * Created by Administrator on 2018/2/14.
 */
//首选项键
object PreferenceKey{
    const val MODE = "MODE"
    const val IS_CHECKED = "IS_CHECKED"
    const val PROGRESS = "PROGRESS"
    const val THEME = "THEME"
}

//首选项
object Preference{
    var mVibrateMode:Int by DelegatesExt.preference(App.instance,PreferenceKey.MODE, VibratorUtil.INTERRUPT)
    var isChecked:Boolean by DelegatesExt.preference(App.instance,PreferenceKey.IS_CHECKED, false)
    var mProgress:Int by DelegatesExt.preference(App.instance,PreferenceKey.PROGRESS, 5)
    var mTheme:Int by DelegatesExt.preference(App.instance,PreferenceKey.THEME, 0)
}