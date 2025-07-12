package com.github.xiaofei_dev.vibrator.util

import android.media.AudioAttributes
import android.os.Build
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

    //通过设置半个小时时长来模拟持续不停地震动
    //private var mDuration = (1000 * 60 * 30).toLong()
    private val mPatternKeep = longArrayOf(1, (1000 * 10).toLong(), 1, (1000 * 10).toLong())
    var isVibrate: Boolean = false
        private set

    private val mAudioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM) //key
            .build()

    //开始震动
    fun vibrate(mode: Int) {
        Log.d(TAG, "vibrate:0 ")
        isVibrate = true
        val pattern = if (mode == INTERRUPT) mPattern else mPatternKeep

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // API 26+
            val effect = android.os.VibrationEffect.createWaveform(pattern, 0)

            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> { // API 33+
                    val attributes = android.os.VibrationAttributes.Builder()
                        .setUsage(android.os.VibrationAttributes.USAGE_ALARM)
                        .build()
                    mVibrator.vibrate(effect, attributes)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> { // API 29-32
                    @Suppress("DEPRECATION")
                    mVibrator.vibrate(effect, mAudioAttributes)
                }
                else -> { // API 26-28
                    @Suppress("DEPRECATION")
                    mVibrator.vibrate(effect)
                }
            }
        } else { // Below API 26
            @Suppress("DEPRECATION")
            mVibrator.vibrate(pattern, 0)
        }
    }

    fun stopVibrate() {
        isVibrate = false
        mVibrator.cancel()
    }
}
