package com.github.xiaofei_dev.vibrator.singleton

import com.github.xiaofei_dev.vibrator.util.VibratorUtil
import com.xiaofeidev.delegatedemo.delegates.SPDelegates

/**
 * Created by Administrator on 2018/2/14.
 */

//首选项键和首选项
object Preference{
    private const val MODE = "MODE"
    private const val IS_CHECKED = "IS_CHECKED"
    private const val PROGRESS = "PROGRESS"
    private const val THEME = "THEME"

    var mVibrateMode:Int by SPDelegates(MODE, VibratorUtil.INTERRUPT)
    var isChecked:Boolean by SPDelegates(IS_CHECKED, false)
    var mProgress:Int by SPDelegates(PROGRESS, 40)
    var mTheme:Int by SPDelegates(THEME, 0)
}

//进程状态
object AppStatus{
    //用于防止主界面切换主题的时候通知闪烁
    var mNotThemeChange = true
}