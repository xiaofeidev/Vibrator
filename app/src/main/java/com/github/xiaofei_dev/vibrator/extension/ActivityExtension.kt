package com.github.xiaofei_dev.vibrator.extension

import android.app.Activity
import android.graphics.Point

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