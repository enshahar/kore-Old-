package ein2b.core.entity.field

import kore.data.Data
import kore.data.field.Field
import kore.data.task.DefaultFactoryTask
import kotlin.reflect.KClass

class EnumField<ENUM: Enum<ENUM>>(val enums:Array<ENUM>): Field<ENUM>(){
    companion object{
        val fields:HashMap<KClass<out Enum<*>>, EnumField<out Enum<*>>> = hashMapOf()
        inline fun <reified ENUM: Enum<ENUM>> get(type:KClass<out ENUM>, enums:Array<ENUM>?): EnumField<ENUM> {
            @Suppress("UNCHECKED_CAST")
            return (fields[type] ?: EnumField(enums!!).also { fields[type] = it }) as EnumField<ENUM>
        }
    }
}
class EnumListField<ENUM: Enum<ENUM>>(val enums:Array<ENUM>): Field<MutableList<ENUM>>(){
    companion object{
        val fields:HashMap<KClass<out Enum<*>>, EnumListField<out Enum<*>>> = hashMapOf()
        inline fun <reified ENUM: Enum<ENUM>> get(type:KClass<out ENUM>, enums:Array<ENUM>?): EnumListField<ENUM> {
            @Suppress("UNCHECKED_CAST")
            return (fields[type] ?: EnumListField(enums!!).also { fields[type] = it }) as EnumListField<ENUM>
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->List<ENUM>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
class EnumMapField<ENUM: Enum<ENUM>>(val enums:Array<ENUM>): Field<MutableMap<String, ENUM>>(){
    companion object{
        val fields:HashMap<KClass<out Enum<*>>, EnumMapField<out Enum<*>>> = hashMapOf()
        inline fun <reified ENUM: Enum<ENUM>> get(type:KClass<out ENUM>, enums:Array<ENUM>?): EnumMapField<ENUM> {
            @Suppress("UNCHECKED_CAST")
            return (fields[type] ?: EnumMapField(enums!!).also { fields[type] = it }) as EnumMapField<ENUM>
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->Map<String, ENUM>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
//Enum이 일련번호와 밀접한 관계가 있다면 이것을 구현한다.
//eQuery에서 param 엔티티에 이것을 구현한 Enum Field가 있다면 rowid로 대체해 주며
//반대로 반환값도 rowid를 Enum으로 매칭해준다.
interface EnumRowid<T>{
    val rowid:T
    fun toDbString() = "$rowid"
}