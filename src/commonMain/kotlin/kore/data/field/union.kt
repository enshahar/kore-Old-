package ein2b.core.entity.field

import kore.data.Union
import kore.data.Data
import kore.data.field.Field
import kore.data.task.DefaultFactoryTask
import kotlin.reflect.KClass

class UnionField<T: Data>(val union: Union<T>): Field<T>(){
    companion object{
        val fields:HashMap<KClass<out Data>, UnionField<out Data>> = hashMapOf()
        inline operator fun <reified ENTITY: Data> get(union: Union<ENTITY>): UnionField<ENTITY> {
            @Suppress("UNCHECKED_CAST")
            return (fields[ENTITY::class] ?: UnionField(union).also { fields[ENTITY::class] = it }) as UnionField<ENTITY>
        }
    }
}
class UnionListField<T: Data>(val union: Union<T>): Field<MutableList<T>>(){
    companion object{
        val fields:HashMap<KClass<out Data>, UnionListField<out Data>> = hashMapOf()
        inline operator fun <reified ENTITY: Data> get(union: Union<ENTITY>): UnionListField<ENTITY> {
            @Suppress("UNCHECKED_CAST")
            return (fields[ENTITY::class] ?: UnionListField(union).also { fields[ENTITY::class] = it }) as UnionListField<ENTITY>
        }
    }
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->List<T>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
class UnionMapField<T: Data>(val union: Union<T>): Field<MutableMap<String, T>>(){
    companion object{
        val fields:HashMap<KClass<out Data>, UnionMapField<out Data>> = hashMapOf()
        inline operator fun <reified ENTITY: Data> get(union: Union<ENTITY>): UnionMapField<ENTITY> {
            @Suppress("UNCHECKED_CAST")
            return (fields[ENTITY::class] ?: UnionMapField(union).also { fields[ENTITY::class] = it }) as UnionMapField<ENTITY>
        }
    }
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->Map<String,T>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}