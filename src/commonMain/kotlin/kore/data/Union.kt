package kore.data

import kotlin.reflect.KClass

abstract class Union<out T: Data>(vararg val factories:()->T){
    private var _type:ArrayList<KClass<*>>? = null
    val type:ArrayList<KClass<*>> get() {
        return _type ?: factories.fold(arrayListOf<KClass<*>>()){ list, item->
            list.add(item()::class)
            list
        }.also{_type = it}
    }
}