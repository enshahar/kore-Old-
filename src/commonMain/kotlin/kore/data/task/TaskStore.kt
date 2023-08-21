package kore.data.task

import kore.data.Data
import kore.data.indexer.Indexer
import kotlin.reflect.KClass

@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
class TaskStore{
    companion object{
        @PublishedApi internal val stores:HashMap<KClass<out Data>, TaskStore> = hashMapOf()
        //@PublishedApi internal inline fun isFirst(data: Data):Boolean =  stores[data::class]?.store?.contains(data._index) != true
        inline fun firstTask(data:Data): Task?{
            val type = data::class
            val taskStore:TaskStore = stores[type]?.also{
                if(data._index in it.store) return null
            } ?: TaskStore().also{stores[type] = it}
            return Task().also{taskStore.store[data._index] = it}
        }
        inline fun <T:Any>default(type:KClass<out Data>, name:String):T?{
            return when(val default = stores[type]?.store?.get(Indexer.get(type, name)())?.default){
                null -> null
                is Function0<*> -> default() as? T
                else -> default as? T
            }
        }
        internal inline fun include(type:KClass<out Data>, name:String):(()->Boolean)?{
            return stores[type]?.store?.get(Indexer.get(type, name)())?.include
        }
//        internal fun getVali(entity: Data, name:String):eVali?{
//            val type = entity::class
//            return stores[type]?.store?.get(Indexer.get(type, name))?.vali
//        }
        internal inline fun setTask(type:KClass<out Data>, name:String):List<(Any)->Any?>?{
            return stores[type]?.store?.get(Indexer.get(type, name)())?.setTasks
        }
        internal inline fun getTask(type:KClass<out Data>, name:String):List<(Any)->Any?>?{
            return stores[type]?.store?.get(Indexer.get(type, name)())?.getTasks
        }
    }
    @PublishedApi internal var store:HashMap<Int, Task> = hashMapOf()
}