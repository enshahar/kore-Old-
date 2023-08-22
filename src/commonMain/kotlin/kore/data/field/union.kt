@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")

package kore.data.field

import kore.data.Data
import kore.data.Union
import kotlin.reflect.KClass


class UnionField<DATA: Data>(val union: Union<DATA>): Field<DATA>(){
    companion object{
        @PublishedApi internal val fields:HashMap<KClass<out Data>, UnionField<out Data>> = hashMapOf()
        inline operator fun <reified DATA: Data> get(union: Union<DATA>): UnionField<DATA> {
            return fields.getOrPut(DATA::class){UnionField(union)} as UnionField<DATA>
        }
    }
}
class UnionListField<DATA: Data>(val union: Union<DATA>): Field<MutableList<DATA>>(){
    companion object{
        @PublishedApi internal val fields:HashMap<KClass<out Data>, UnionListField<out Data>> = hashMapOf()
        inline operator fun <reified DATA: Data> get(union: Union<DATA>): UnionListField<DATA> {
            return fields.getOrPut(DATA::class){UnionListField(union)} as UnionListField<DATA>
        }
    }
    inline fun Data.default(noinline factory:(Data)->List<DATA>){
        _task?.default = factory
    }
}
class UnionMapField<DATA: Data>(val union: Union<DATA>): Field<MutableMap<String, DATA>>(){
    companion object{
        @PublishedApi internal val fields:HashMap<KClass<out Data>, UnionMapField<out Data>> = hashMapOf()
        inline operator fun <reified DATA: Data> get(union: Union<DATA>): UnionMapField<DATA> {
            return fields.getOrPut(DATA::class){UnionMapField(union)} as UnionMapField<DATA>
        }
    }
    inline fun Data.default(noinline factory:(Data)->Map<String,DATA>){
        _task?.default = factory
    }
}