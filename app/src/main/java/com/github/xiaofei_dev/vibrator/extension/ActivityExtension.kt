package com.github.xiaofei_dev.vibrator.extension

import android.app.Activity
import android.content.Context
import android.graphics.Point
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
fun Activity.setTheme(context: Context) {
    when (Preference.mTheme) {
        R.style.AppTheme_Red,
        R.style.AppTheme_Pink,
        R.style.AppTheme_Yellow,
        R.style.AppTheme_Green,
        R.style.AppTheme_Blue,
        R.style.AppTheme_Black -> context.setTheme(Preference.mTheme)
        else -> context.setTheme(R.style.AppTheme)
    }
}