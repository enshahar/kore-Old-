package kore.data.task

import kore.data.Data
import kore.data.indexer.Indexer
import kotlin.reflect.KClass

@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
class TaskStore{
    companion object{
        @PublishedApi internal val stores:HashMap<KClass<out Data>, TaskStore> = hashMapOf()
        inline fun firstTask(data:Data): Task?{
            val type = data::class
            val taskStore:TaskStore = stores[type]?.also{
                if(data._index in it.store) return null
            } ?: TaskStore().also{stores[type] = it}
            return Task().also{taskStore.store[data._index] = it}
        }
        internal inline operator fun invoke(type:KClass<out Data>, name:String):Task? = stores[type]!!.store[Indexer.get(type, name)()]
        internal inline operator fun invoke(type:KClass<out Data>, index:Int):Task? = stores[type]!!.store[index]
    }
    @PublishedApi internal var store:HashMap<Int, Task> = hashMapOf()
}