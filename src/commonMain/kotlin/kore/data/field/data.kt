@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package kore.data.field

import kore.data.Data
import kotlin.reflect.KClass

class DataField<DATA: Data>(val cls: KClass<DATA>, val factory:()->DATA): Field<DATA>(){
    companion object{
        @PublishedApi internal val fields:HashMap<KClass<out Data>, DataField<out Data>> = hashMapOf()
        inline operator fun <reified DATA: Data> get(noinline factory:()->DATA): DataField<DATA> {
            return fields.getOrPut(DATA::class){DataField(DATA::class, factory)} as DataField<DATA>
        }
        inline operator fun <DATA: Data> get(cls:KClass<DATA>, noinline factory:()->DATA): DataField<DATA> {
            return fields.getOrPut(cls){DataField(cls, factory)} as DataField<DATA>
        }
    }
    inline fun Data.default(noinline factory:(Data)-> DATA){
        _task?.default = factory
    }
}
class DataListField<DATA: Data>(val cls: KClass<DATA>, val factory:()->DATA): Field<MutableList<DATA>>(){
    companion object{
        @PublishedApi internal val fields:HashMap<KClass<out Data>, DataListField<out Data>> = hashMapOf()
        inline operator fun <reified DATA: Data> get(noinline factory:()->DATA): DataListField<DATA> {
            return fields.getOrPut(DATA::class){DataListField(DATA::class, factory)} as DataListField<DATA>
        }
    }
    inline fun Data.default(noinline factory:(Data)->List<DATA>){
        _task?.default = factory
    }
}
class DataMapField<DATA: Data>(val cls: KClass<DATA>, val factory:()->DATA): Field<MutableMap<String, DATA>>(){
    companion object{
        @PublishedApi internal val fields:HashMap<KClass<out Data>, DataMapField<out Data>> = hashMapOf()
        inline operator fun <reified DATA: Data> get(noinline factory:()->DATA): DataMapField<DATA> {
            return fields.getOrPut(DATA::class){DataMapField(DATA::class, factory)} as DataMapField<DATA>
        }
        inline operator fun <DATA: Data> get(cls:KClass<DATA>, noinline factory:()->DATA): DataMapField<DATA> {
            return fields.getOrPut(cls){DataMapField(cls, factory)} as DataMapField<DATA>
        }
    }
    inline fun Data.default(noinline factory:(Data)-> Map<String,DATA>){
        _task?.default = factory
    }
}