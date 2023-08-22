@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")

package kore.data.field

import kore.data.Data
import kotlin.reflect.KClass

class EnumField<ENUM: Enum<ENUM>>(val enums:Array<ENUM>): Field<ENUM>(){
    companion object{
        @PublishedApi internal val fields:HashMap<KClass<out Enum<*>>, EnumField<out Enum<*>>> = hashMapOf()
        inline operator fun <reified ENUM: Enum<ENUM>> invoke(): EnumField<ENUM> {
            return fields.getOrPut(ENUM::class){EnumField(enumValues<ENUM>())} as EnumField<ENUM>
        }
    }
}
class EnumListField<ENUM: Enum<ENUM>>(val enums:Array<ENUM>): Field<MutableList<ENUM>>(){
    companion object{
        @PublishedApi internal val fields:HashMap<KClass<out Enum<*>>, EnumListField<out Enum<*>>> = hashMapOf()
        inline operator fun <reified ENUM: Enum<ENUM>> invoke(): EnumListField<ENUM> {
            return fields.getOrPut(ENUM::class){EnumListField(enumValues<ENUM>())} as EnumListField<ENUM>
        }
    }
    inline fun Data.default(noinline factory:(Data)->List<ENUM>){
        _task?.default = factory
    }
}
class EnumMapField<ENUM: Enum<ENUM>>(val enums:Array<ENUM>): Field<MutableMap<String, ENUM>>(){
    companion object{
        @PublishedApi internal val fields:HashMap<KClass<out Enum<*>>, EnumMapField<out Enum<*>>> = hashMapOf()
        inline operator fun <reified ENUM: Enum<ENUM>> invoke(): EnumMapField<ENUM> {
            return fields.getOrPut(ENUM::class){EnumMapField(enumValues<ENUM>())} as EnumMapField<ENUM>
        }
    }
    inline fun Data.default(noinline factory:(Data)->Map<String, ENUM>){
        _task?.default = factory
    }
}
/**
 * Enum이 일련번호와 밀접한 관계가 있다면 이것을 구현한다.
 * eQuery에서 param 엔티티에 이것을 구현한 Enum Field가 있다면 rowid로 대체해 주며
 * 반대로 반환값도 rowid를 Enum으로 매칭해준다.
 */
interface EnumRowid<T>{
    val rowid:T
    fun toDbString() = "$rowid"
}