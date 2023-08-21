package kore.data.indexer

import kore.data.Data
import kore.r.R
import kotlin.reflect.KClass

@Suppress("NOTHING_TO_INLINE")
object Indexer{
    @PublishedApi internal val indexes:HashMap<KClass<out Data>, HashMap<String, Int>> = hashMapOf()
    fun set(data: Data, name:String, i: Int){
        val index = data::class.let{type->
            indexes[type] ?: hashMapOf<String, Int>().also{ indexes[type] = it }
        }
        if(name !in index) index[name] = i
    }
    inline fun get(data: KClass<out Data>, name:String): R<Int> = R(indexes[data]?.get(name) ?: Data.NoIndex(name))
}