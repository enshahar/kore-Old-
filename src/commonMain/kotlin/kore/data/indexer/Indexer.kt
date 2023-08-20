package ein2b.core.entity.indexer

import ein2b.core.entity.Error
import kore.data.Data
import kotlin.reflect.KClass

object Indexer{
    private val indexes:HashMap<KClass<out Data>, HashMap<String, Int>> = hashMapOf()

    fun set(entity: Data, name:String, i: Int){
        val index = entity::class.let{type->
            indexes[type] ?: hashMapOf<String, Int>().also{ indexes[type] = it }
        }
        if(name !in index) index[name] = i
    }
    fun get(entity: KClass<out Data>, name:String):Int = indexes[entity]?.get(name) ?: throw Error(Data.ERROR.index_error, "no index:${entity.simpleName}.$name")
    fun getOrNull(entity: KClass<out Data>, name:String):Int? = indexes[entity]?.get(name)
}