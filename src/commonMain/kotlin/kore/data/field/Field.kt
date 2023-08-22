@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package kore.data.field

import kore.data.Data
import kore.data.SlowData
import kore.data.indexer.Indexer
import kore.data.task.Task
import kotlin.jvm.JvmInline
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass

// 코딩시 주의사항:
//  필드는 같은 타입에 대해 싱글턴으로 단 하나만 생기므로 필드안에 개별 객체 수준의 정보나 클래스 수준의 정보가
//  들어가면 안된다. 객체 수준 정보는 엔티티 객체를 참조하고, 클래스 수준의 정보는 클래스 스토어를 참조하는 식으로 처리해야 한다.
abstract class Field<VALUE:Any>{
    companion object{
        @Suppress("NOTHING_TO_INLINE")
        inline fun isNotObjectField(cls:KClass<*>):Boolean = cls !in objectField
        val objectField:HashSet<KClass<*>> = hashSetOf(
            DataField::class, DataListField::class, DataMapField::class,
            UnionField::class, UnionListField::class, UnionMapField::class
        )
        val isInclude:Data.()->Boolean = { true }
        val isNotInclude:Data.()->Boolean = { false }
        val isOptional:Data.()->Boolean = { true }
        private val fields:HashMap<KClass<out Data>, HashMap<String, Field<*>>> = hashMapOf()
        operator fun get(cls:KClass<out Data>):HashMap<String, Field<*>>? = fields[cls]
        operator fun set(cls:KClass<out Data>, newMap: HashMap<String, Field<*>>) {
            if(cls !in fields) fields[cls] = newMap
        }
    }
    val delegator = PropertyDelegateProvider<Data, ReadWriteProperty<Data, VALUE>>{ data, prop->
        val name: String = prop.name
        val type: KClass<out Data> = data::class
        val slowData: SlowData? = data as? SlowData
        val field: HashMap<String, Field<*>> = slowData?._fields ?: fields.getOrPut(type){hashMapOf()}
        if(name !in field){
            field[name] = this
            Indexer.set(data::class, name, data._index++)
            if(data._values == null) data._values = hashMapOf()
        }
        data as ReadWriteProperty<Data, VALUE>
    }
    inline fun Data.default(value:VALUE){
        _task?.default = when(value){
            is Number, is String, is Boolean, is Enum<*>->value
            else->Data.DefaultNotValue(value).terminate()
        }
    }
    inline fun Data.default(value: Data.Immutable<VALUE>){
        _task?.default = value.value
    }
    @JvmInline
    value class Encoding(val task: Task?){
        inline fun isExcluded(){
            task?.run{ include = isNotInclude }
        }
        inline fun isOptional(){
            task?.run{ include = isOptional }
        }
        inline fun setResolver(noinline block:Data.()->Boolean){
            task?.run{ include = block }
        }
    }
    inline val Data.encoding: Encoding get() = Encoding(_task)
//    inline fun Data.validator(vali:eVali){
//        _task?.run{ this.vali = vali }
//    }
    inline fun Data.get(noinline block:(Data, Any)->Any?){
        _task?.let{ task ->
            (task.getTasks ?: arrayListOf<(Data, Any)->Any?>().also{task.getTasks = it}).add(block)
        }
    }
    inline fun Data.set(noinline block:(Data, Any)->Any?){
        _task?.let{ task ->
            (task.setTasks ?: arrayListOf<(Data, Any)->Any?>().also{task.setTasks = it}).add(block)
        }
    }
}
object IntField: Field<Int>()
object UIntField: Field<UInt>()
object LongField: Field<Long>()
object ULongField: Field<ULong>()
object ShortField: Field<Short>()
object UShortField: Field<UShort>()
object FloatField: Field<Float>()
object DoubleField: Field<Double>()
object BooleanField: Field<Boolean>()
object StringField: Field<String>()
//object UtcField: Field<eUtc>() {
//    @Suppress("NOTHING_TO_INLINE")
//    inline fun Data.default(noinline factory:(Data)->eUtc){
//        _defaultMap = _defaultMap ?: hashMapOf()
//        _defaultMap!![_index] = DefaultFactoryTask(factory)
//    }
//}

object IntListField: Field<MutableList<Int>>(){
    inline fun Data.default(noinline factory:(Data)->List<Int>){
        _task?.default = factory
    }
}
object UIntListField: Field<MutableList<UInt>>(){
    inline fun Data.default(noinline factory:(Data)->List<UInt>){
        _task?.default = factory
    }
}
object LongListField: Field<MutableList<Long>>(){
    inline fun Data.default(noinline factory:(Data)->List<Long>){
        _task?.default = factory
    }
}
object ULongListField: Field<MutableList<ULong>>(){
    inline fun Data.default(noinline factory:(Data)->List<ULong>){
        _task?.default = factory
    }
}
object ShortListField: Field<MutableList<Short>>(){
    inline fun Data.default(noinline factory:(Data)->List<Short>){
        _task?.default = factory
    }
}
object UShortListField: Field<MutableList<UShort>>(){
    inline fun Data.default(noinline factory:(Data)->List<UShort>){
        _task?.default = factory
    }
}
object FloatListField: Field<MutableList<Float>>(){
    inline fun Data.default(noinline factory:(Data)->List<Float>){
        _task?.default = factory
    }
}
object DoubleListField: Field<MutableList<Double>>(){
    inline fun Data.default(noinline factory:(Data)->List<Double>){
        _task?.default = factory
    }
}
object BooleanListField: Field<MutableList<Boolean>>(){
    inline fun Data.default(noinline factory:(Data)->List<Boolean>){
        _task?.default = factory
    }
}
object StringListField: Field<MutableList<String>>(){
    inline fun Data.default(noinline factory:(Data)->List<String>){
        _task?.default = factory
    }
}

object IntMapField: Field<HashMap<String, Int>>(){
    inline fun Data.default(noinline factory:(Data)->Map<String, Int>){
        _task?.default = factory
    }
}
object UIntMapField: Field<HashMap<String, UInt>>(){
    inline fun Data.default(noinline factory:(Data)->Map<String, UInt>){
        _task?.default = factory
    }
}
object LongMapField: Field<HashMap<String, Long>>(){
    inline fun Data.default(noinline factory:(Data)->Map<String, Long>){
        _task?.default = factory
    }
}
object ULongMapField: Field<HashMap<String, ULong>>(){
    inline fun Data.default(noinline factory:(Data)->Map<String, ULong>){
        _task?.default = factory
    }
}
object ShortMapField: Field<HashMap<String, Short>>(){
    inline fun Data.default(noinline factory:(Data)->Map<String, Short>){
        _task?.default = factory
    }
}
object UShortMapField: Field<HashMap<String, UShort>>(){
    inline fun Data.default(noinline factory:(Data)->Map<String, UShort>){
        _task?.default = factory
    }
}
object FloatMapField: Field<HashMap<String, Float>>(){
    inline fun Data.default(noinline factory:(Data)->Map<String, Float>){
        _task?.default = factory
    }
}
object DoubleMapField: Field<HashMap<String, Double>>(){
    inline fun Data.default(noinline factory:(Data)->Map<String, Double>){
        _task?.default = factory
    }
}
object BooleanMapField: Field<HashMap<String, Boolean>>(){
    inline fun Data.default(noinline factory:(Data)->Map<String, Boolean>){
        _task?.default = factory
    }
}
object StringMapField: Field<HashMap<String, String>>(){
    inline fun Data.default(noinline factory:(Data)->Map<String, String>){
        _task?.default = factory
    }
}