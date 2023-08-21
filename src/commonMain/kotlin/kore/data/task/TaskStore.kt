package kore.data.task

import kore.data.Data
import kore.data.indexer.Indexer
import kotlin.reflect.KClass

@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
class TaskStore{
    companion object{
        private val stores:HashMap<KClass<out Data>, TaskStore> = hashMapOf()
        private inline fun isFirst(data: Data):Boolean =  stores[data::class]?.store?.contains(data._index) == true
        fun getFirstTasks(data:Data): Tasks?{
            if(!isFirst(data)) return stores[data::class]?.store?.get(data._index)
            val taskStore = stores[data::class] ?: TaskStore().also{stores[data::class] = it}
            if(taskStore.maxIndex >= data._index) return null
            taskStore.maxIndex = data._index
            return if(data._index in taskStore.store) null else Tasks().also{taskStore.store[data._index] = it}
        }
        fun <T:Any>default(data:Data, name:String):T?{
            val type = data::class
            return when(val default = stores[type]?.store?.get(Indexer.get(type, name)())?.default){
                null -> null
                is Function0<*> -> default() as? T
                else -> default as? T
            }
        }
        internal fun include(data: Data, name:String):(()->Boolean)?{
            val type = data::class
            return stores[type]?.store?.get(Indexer.get(type, name)())?.include
        }
//        internal fun getVali(entity: Data, name:String):eVali?{
//            val type = entity::class
//            return stores[type]?.store?.get(Indexer.get(type, name))?.vali
//        }
        internal fun setTask(data: Data, name:String):List<(Any)->Any?>?{
            val type = data::class
            return stores[type]?.store?.get(Indexer.get(type, name)())?.setTasks
        }
        internal fun getTask(data: Data, name:String):List<(Any)->Any?>?{
            val type = data::class
            return stores[type]?.store?.get(Indexer.get(type, name)())?.getTasks
        }
    }
    private var maxIndex = -1
    private var store:HashMap<Int, Tasks> = hashMapOf()
}