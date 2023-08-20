package ein2b.core.entity.field

import kore.data.Data
import kore.data.eSlowEntity
import ein2b.core.entity.task.DefaultFactoryTask
import kotlin.reflect.KClass

open class EntityField<T: Data>(val factory:()->T): Field<T>(){
    companion object{
        val fields:HashMap<KClass<out Data>, EntityField<out Data>> = hashMapOf()
        inline operator fun <reified ENTITY: Data> get(noinline factory:()->ENTITY): EntityField<ENTITY> {
            @Suppress("UNCHECKED_CAST")
            return (fields[ENTITY::class] ?: EntityField(factory).also { fields[ENTITY::class] = it }) as EntityField<ENTITY>
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()-> T){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
open class EntityListField<T: Data>(val factory:()->T): Field<MutableList<T>>(){
    companion object{
        val fields:HashMap<KClass<out Data>, EntityListField<out Data>> = hashMapOf()
        inline operator fun <reified ENTITY: Data> get(noinline factory:()->ENTITY): EntityListField<ENTITY> {
            @Suppress("UNCHECKED_CAST")
            return (fields[ENTITY::class] ?: EntityListField(factory).also { fields[ENTITY::class] = it }) as EntityListField<ENTITY>
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->List<T>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
open class EntityMapField<T: Data>(val factory:()->T): Field<MutableMap<String, T>>(){
    companion object{
        val fields:HashMap<KClass<out Data>, EntityMapField<out Data>> = hashMapOf()
        inline operator fun <reified ENTITY: Data> get(noinline factory:()->ENTITY): EntityMapField<ENTITY> {
            @Suppress("UNCHECKED_CAST")
            return (fields[ENTITY::class] ?: EntityMapField(factory).also { fields[ENTITY::class] = it }) as EntityMapField<ENTITY>
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()-> Map<String,T>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}

class SlowEntityField<T: Data>(val cls: KClass<T>, factory:()->T): EntityField<T>(factory){
    companion object{
        val fields:HashMap<KClass<out Data>, SlowEntityField<out Data>> = hashMapOf()
        inline operator fun <ENTITY: Data> get(cls:KClass<ENTITY>, noinline factory:()->ENTITY): SlowEntityField<ENTITY> {
            @Suppress("UNCHECKED_CAST")
            return (fields[cls] ?: SlowEntityField(cls,factory).also { fields[cls] = it }) as SlowEntityField<ENTITY>
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun <E: Data> eSlowEntity.default(noinline factory:()-> E){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
class SlowEntityListField<T: Data>(val cls:KClass<T>, factory:()->T): EntityListField<T>(factory){
    companion object{
        val fields:HashMap<KClass<out Data>, SlowEntityListField<out Data>> = hashMapOf()
        inline operator fun <ENTITY: Data> get(cls:KClass<ENTITY>, noinline factory:()->ENTITY): SlowEntityListField<ENTITY> {
            @Suppress("UNCHECKED_CAST")
            return (fields[cls] ?: SlowEntityListField(cls,factory).also { fields[cls] = it }) as SlowEntityListField<ENTITY>
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun <E: Data> eSlowEntity.default(noinline factory:()->List<E>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
class SlowEntityMapField<T: Data>(val cls:KClass<T>, factory:()->T): EntityMapField<T>(factory){
    companion object{
        val fields:HashMap<KClass<out Data>, SlowEntityMapField<out Data>> = hashMapOf()
        inline operator fun <ENTITY: Data> get(cls: KClass<ENTITY>, noinline factory:()->ENTITY): SlowEntityMapField<ENTITY> {
            @Suppress("UNCHECKED_CAST")
            return (fields[cls] ?: SlowEntityMapField(cls, factory).also { fields[cls] = it }) as SlowEntityMapField<ENTITY>
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun <E: Data> eSlowEntity.default(noinline factory:()-> Map<String,E>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}