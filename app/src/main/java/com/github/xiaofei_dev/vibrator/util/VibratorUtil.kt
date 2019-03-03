package com.github.xiaofei_dev.vibrator.util

import android.os.Vibrator
import android.util.Log


/**
 * author：xiaofei_dev
 * time：2017/5/15:8:47
 * e-mail：xiaofei.dev@gmail.com
 * desc：coding
 */
//此类用于描述振动属性，即Vibrator类的方法参数值
class VibratorUtil(private val mVibrator: Vibrator) {
    companion object {
        private val TAG = "VibratorUtil"
        //振动模式为断续
        val INTERRUPT = 0
        //振动模式为持续
        val KEEP = 1
        var mPattern = longArrayOf(0, 0, 0)
    }

    //通过设置一个小时时长来模拟持续不停地震动
    private var mDuration = (1000 * 60 * 60).toLong()
    private val mPatternKeep = longArrayOf(1, (1000 * 10).toLong(), 1, (1000 * 10).toLong())
    var isVibrate: Boolean = false
        private set

    fun setDuration(duration: Long) {
        mDuration = duration
    }

    //开始震动
    fun vibrate(mode: Int) {
        Log.d(TAG, "vibrate:0 ")
        isVibrate = true
        when (mode) {
            INTERRUPT -> {
                mVibrator.vibrate(mPattern, 0)
                Log.d(TAG, "vibrate:0 ")
            }
            KEEP ->
//                mVibrator.vibrate(mDuration);
                mVibrator.vibrate(mPatternKeep, 0)
            else -> {
            }
        }
    }

    fun stopVibrate() {
        isVibrate = false
        mVibrator.cancel()
    }
}
