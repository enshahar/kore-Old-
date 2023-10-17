package kore.data.indexer

import kore.data.VO
import kore.wrap.W
import kore.wrap.Wrap
import kotlin.reflect.KClass

@Suppress("NOTHING_TO_INLINE")
object Indexer{
    @PublishedApi internal val indexes:HashMap<KClass<out VO>, HashMap<String, Int>> = hashMapOf()
    inline fun set(type:KClass<out VO>, name:String, i: Int){
        indexes.getOrPut(type){hashMapOf()}[name] = i
    }
    inline fun get(type:KClass<out VO>, name:String): Wrap<Int> = indexes[type]?.get(name)?.let{ W(it) } ?: W(VO.NoIndex(name))
}