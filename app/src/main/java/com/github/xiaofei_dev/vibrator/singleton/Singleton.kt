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
    private const val PURCHASE = "PURCHASE"

    var mVibrateMode:Int by SPDelegates(MODE, VibratorUtil.INTERRUPT)
    var isChecked:Boolean by SPDelegates(IS_CHECKED, false)
    var mProgress:Int by SPDelegates(PROGRESS, 40)
    var mTheme:Int by SPDelegates(THEME, 0)
    //当前用户的应用内购状态
    var mPurchaseStatus: Int by SPDelegates(PURCHASE, PurchaseStatus.UNKNOWN)
}

//进程状态
object AppStatus{
    //用于防止主界面切换主题的时候通知闪烁
    var mNotThemeChange = true
}

//内购状态
object PurchaseStatus{
    const val UNKNOWN = 0//未知
    const val BOUGHT = 1//已购买
    const val NO_BOUGHT = 2//未购买
}