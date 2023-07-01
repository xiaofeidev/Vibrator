package com.github.xiaofei_dev.vibrator.extension

import android.app.Activity
import android.content.Context
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.github.xiaofei_dev.vibrator.R
import com.github.xiaofei_dev.vibrator.singleton.Preference

/**
 * Created by Administrator on 2018/2/20.
 */
//Activity 的扩展属性，屏幕宽度
val Activity.screenWidth:Int
    get(){
        //获取屏幕尺寸,这里主要使用宽度
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return  size.x
    }

//Activity 的扩展属性，屏幕高度
val Activity.screenHeight:Int
    get(){
        //获取屏幕尺寸,这里主要使用宽度
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return  size.y
    }

//设置主题
fun AppCompatActivity.setTheme() {
    when (Preference.mTheme) {
        R.style.AppTheme_Red,
        R.style.AppTheme_Pink,
        R.style.AppTheme_Yellow,
        R.style.AppTheme_Green,
        R.style.AppTheme_Blue,
        R.style.AppTheme_Black -> setTheme(Preference.mTheme)
        else -> setTheme(R.style.AppTheme)
    }

    delegate.applyDayNight()
}

fun AppCompatActivity.hideSystemUINew() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}