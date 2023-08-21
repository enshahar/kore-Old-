package kore.data.indexer

import kore.data.Data
import kore.wrap.W
import kore.wrap.Wrap
import kotlin.reflect.KClass

@Suppress("NOTHING_TO_INLINE")
object Indexer{
    @PublishedApi internal val indexes:HashMap<KClass<out Data>, HashMap<String, Int>> = hashMapOf()
    inline fun set(type:KClass<out Data>, name:String, i: Int){
        (indexes[type] ?: hashMapOf<String, Int>().also{indexes[type] = it})[name] = i
    }
    inline fun get(type:KClass<out Data>, name:String): Wrap<Int> = indexes[type]?.get(name)?.let{ W(it) } ?: W(Data.NoIndex(name))
}