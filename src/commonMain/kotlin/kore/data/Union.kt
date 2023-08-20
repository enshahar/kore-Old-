package ein2b.core.entity

import kore.data.Data
import kotlin.reflect.KClass

abstract class Union<out T: Data>(vararg val factories:()->T){
    private var types:ArrayList<KClass<*>>? = null
    val type:ArrayList<KClass<*>> get() {
        if(types == null){
            types = arrayListOf()
            factories.forEach { types?.add(it()::class) }
        }
        return types!!
    }
}