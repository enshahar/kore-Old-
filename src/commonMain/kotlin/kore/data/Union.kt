package kore.data

import kotlin.reflect.KClass

abstract class Union<out T: Data>(vararg val factories:()->T){
    private var _type:List<KClass<*>>? = null
    val type:List<KClass<*>> get() = _type ?: factories.map{it()::class}.also{_type = it}
}