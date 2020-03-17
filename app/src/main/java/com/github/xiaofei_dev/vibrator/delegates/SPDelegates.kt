package com.xiaofeidev.delegatedemo.delegates

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author xiaofei_dev
 * @desc <p>读写 SP 存储项的轻量级委托类，如下，
 * 读 SP 的操作委托给该类对象的 getValue 方法，
 * 写 SP 操作委托给该类对象的 setValue 方法，
 * 注意这两个方法不用你显式调用，把一切交给编译器就行
 * 具体使用此类定义 SP 存储项的代码请参考 SpBase 文件</p>
 */

class SPDelegates<T>(private val key: String, private val default: T) : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return SPUtils.getValue(key, default)
    }
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        SPUtils.putValue(key, value)
    }
}
