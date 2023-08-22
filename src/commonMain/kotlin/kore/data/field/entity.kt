@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package kore.data.field

import kore.data.Data
import kore.data.SlowData
import kotlin.reflect.KClass

open class EntityField<T: Data>(val factory:()->T): Field<T>(){
    companion object{
        val fields:HashMap<KClass<out Data>, EntityField<out Data>> = hashMapOf()
        inline operator fun <reified ENTITY: Data> get(noinline factory:()->ENTITY): EntityField<ENTITY> {
            return (fields[ENTITY::class] ?: EntityField(factory).also { fields[ENTITY::class] = it }) as EntityField<ENTITY>
        }
    }
    inline fun Data.default(noinline factory:(Data)-> T){
        _task?.default = factory
    }
}
open class EntityListField<T: Data>(val factory:()->T): Field<MutableList<T>>(){
    companion object{
        val fields:HashMap<KClass<out Data>, EntityListField<out Data>> = hashMapOf()
        inline operator fun <reified ENTITY: Data> get(noinline factory:()->ENTITY): EntityListField<ENTITY> {
            return (fields[ENTITY::class] ?: EntityListField(factory).also { fields[ENTITY::class] = it }) as EntityListField<ENTITY>
        }
    }
    inline fun Data.default(noinline factory:(Data)->List<T>){
        _task?.default = factory
    }
}
open class EntityMapField<T: Data>(val factory:()->T): Field<MutableMap<String, T>>(){
    companion object{
        val fields:HashMap<KClass<out Data>, EntityMapField<out Data>> = hashMapOf()
        inline operator fun <reified ENTITY: Data> get(noinline factory:()->ENTITY): EntityMapField<ENTITY> {
            return (fields[ENTITY::class] ?: EntityMapField(factory).also { fields[ENTITY::class] = it }) as EntityMapField<ENTITY>
        }
    }
    inline fun Data.default(noinline factory:(Data)-> Map<String,T>){
        _task?.default = factory
    }
}

class SlowEntityField<T: Data>(val cls: KClass<T>, factory:()->T): EntityField<T>(factory){
    companion object{
        val fields:HashMap<KClass<out Data>, SlowEntityField<out Data>> = hashMapOf()
        inline operator fun <ENTITY: Data> get(cls:KClass<ENTITY>, noinline factory:()->ENTITY): SlowEntityField<ENTITY> {
            return (fields[cls] ?: SlowEntityField(cls,factory).also { fields[cls] = it }) as SlowEntityField<ENTITY>
        }
    }
    inline fun <E: Data> SlowData.default(noinline factory:(Data)-> E){
        _task?.default = factory
    }
}
class SlowEntityListField<T: Data>(val cls:KClass<T>, factory:()->T): EntityListField<T>(factory){
    companion object{
        val fields:HashMap<KClass<out Data>, SlowEntityListField<out Data>> = hashMapOf()
        inline operator fun <ENTITY: Data> get(cls:KClass<ENTITY>, noinline factory:()->ENTITY): SlowEntityListField<ENTITY> {
            return (fields[cls] ?: SlowEntityListField(cls,factory).also { fields[cls] = it }) as SlowEntityListField<ENTITY>
        }
    }
    inline fun <E: Data> SlowData.default(noinline factory:(Data)->List<E>){
        _task?.default = factory
    }
}
class SlowEntityMapField<T: Data>(val cls:KClass<T>, factory:()->T): EntityMapField<T>(factory){
    companion object{
        val fields:HashMap<KClass<out Data>, SlowEntityMapField<out Data>> = hashMapOf()
        inline operator fun <ENTITY: Data> get(cls: KClass<ENTITY>, noinline factory:()->ENTITY): SlowEntityMapField<ENTITY> {
            return (fields[cls] ?: SlowEntityMapField(cls, factory).also { fields[cls] = it }) as SlowEntityMapField<ENTITY>
        }
    }
    inline fun <E: Data> SlowData.default(noinline factory:(Data)-> Map<String,E>){
        _task?.default = factory
    }
}