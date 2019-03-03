package com.github.xiaofei_dev.vibrator.extension

/**
 * Created by xiaofei on 2018/12/30.
 * desc:Boolean Extension1, Say Goodbye to if-else expression
 * 为 Boolean 类扩展一套流式 API，详见：https://www.jianshu.com/p/8a54b7802077
 */

sealed class BooleanExt1<out T>////起桥梁作用的中间类，定义成协变

object Otherwise : BooleanExt1<Nothing>()//Nothing是所有类型的子类型，协变的类继承关系和泛型参数类型继承关系一致

class TransferData1<T>(val data: T) : BooleanExt1<T>()//data只涉及到了只读的操作

//声明成inline函数
inline fun <T> Boolean.yes(block: () -> T): BooleanExt1<T> = when {
    this -> {
        TransferData1(block.invoke())
    }
    else -> Otherwise
}

inline fun <T> BooleanExt1<T>.otherwise(block: () -> T): T = when (this) {
    is Otherwise ->
        block()
    is TransferData1 ->
        this.data
}