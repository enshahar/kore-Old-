@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package kore.data.field

import ein2b.core.entity.field.*
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
            EntityField::class, EntityListField::class, EntityMapField::class,
            UnionField::class, UnionListField::class, UnionMapField::class
        )
        val isInclude:()->Boolean = { true }
        val isNotInclude:()->Boolean = { false }
        val isOptional:()->Boolean = { true }
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
        ((this@default as? SlowData)?._tasks?.getOrPut(_index){Task()} ?: _task)?.default = when(value){
            is Number, is String, is Boolean, is Enum<*>->value
            else->Data.DefaultNotValue(value).terminate()
        }
    }
    inline fun Data.default(value: Data.Immutable<VALUE>){
        ((this@default as? SlowData)?._tasks?.getOrPut(_index){Task()} ?: _task)?.default = value.value
    }
    @JvmInline
    value class Encoding(val task: Task?){
        @Suppress("NOTHING_TO_INLINE")
        inline fun isExcluded(){
            task?.run{ include = isNotInclude }
        }
        @Suppress("NOTHING_TO_INLINE")
        inline fun isOptional(){
            task?.run{ include = isOptional }
        }
        @Suppress("NOTHING_TO_INLINE")
        inline fun setResolver(noinline block:()->Boolean){
            task?.run{ include = block }
        }
    }
    inline val Data.encoding: Encoding get() = Encoding((this as? SlowData)?._tasks?.getOrPut(_index){Task()} ?: _task)
//    inline fun Data.validator(vali:eVali){
//        _task?.run{ this.vali = vali }
//    }
    inline fun Data.get(block: FieldGet.()->Unit){
        _task?.run{ FieldGet(this).block() }
    }
    inline fun Data.set(block: FieldSet.()->Unit){
        _task?.run{ FieldSet(this).block() }
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
object UtcField: Field<eUtc>() {
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->eUtc){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}

object IntListField: Field<MutableList<Int>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->List<Int>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
object UIntListField: Field<MutableList<UInt>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->List<UInt>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
object LongListField: Field<MutableList<Long>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->List<Long>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
object ULongListField: Field<MutableList<ULong>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->List<ULong>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
object ShortListField: Field<MutableList<Short>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->List<Short>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
object UShortListField: Field<MutableList<UShort>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->List<UShort>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
object FloatListField: Field<MutableList<Float>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->List<Float>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
object DoubleListField: Field<MutableList<Double>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->List<Double>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
object BooleanListField: Field<MutableList<Boolean>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->List<Boolean>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
object StringListField: Field<MutableList<String>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->List<String>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}

object IntMapField: Field<HashMap<String, Int>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->Map<String, Int>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
object UIntMapField: Field<HashMap<String, UInt>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->Map<String, UInt>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
object LongMapField: Field<HashMap<String, Long>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->Map<String, Long>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
object ULongMapField: Field<HashMap<String, ULong>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->Map<String, ULong>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
object ShortMapField: Field<HashMap<String, Short>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->Map<String, Short>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
object UShortMapField: Field<HashMap<String, UShort>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->Map<String, UShort>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
object FloatMapField: Field<HashMap<String, Float>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->Map<String, Float>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
object DoubleMapField: Field<HashMap<String, Double>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->Map<String, Double>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
object BooleanMapField: Field<HashMap<String, Boolean>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->Map<String, Boolean>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}
object StringMapField: Field<HashMap<String, String>>(){
    @Suppress("NOTHING_TO_INLINE")
    inline fun Data.default(noinline factory:()->Map<String, String>){
        //_task?.default = DefaultFactoryTask(factory)
        _defaultMap = _defaultMap ?: hashMapOf()
        _defaultMap!![_index] = DefaultFactoryTask(factory)
    }
}