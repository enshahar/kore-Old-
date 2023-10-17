@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package kore.data.field

import kore.data.VO
import kore.data.indexer.Indexer
import kore.data.task.Task
import kotlin.jvm.JvmInline
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass

//companion object{
//    @Suppress("NOTHING_TO_INLINE")
//    inline fun isNotObjectField(cls:KClass<*>):Boolean = cls !in objectField
//    val objectField:HashSet<KClass<*>> = hashSetOf(
//        DataField::class, DataListField::class, DataMapField::class,
//        UnionField::class, UnionListField::class, UnionMapField::class
//    )
//    val isInclude:VO.()->Boolean = { true }
//    val isNotInclude:VO.()->Boolean = { false }
//    val isOptional:VO.()->Boolean = { true }
//}
//    inline fun VO.default(value:VALUE){
//        _task?.default = when(value){
//            is Number, is String, is Boolean, is Enum<*>->value
//            else->VO.DefaultNotValue(value).terminate()
//        }
//    }
//    inline fun VO.default(value: VO.Immutable<VALUE>){
//        _task?.default = value.value
//    }
//    @JvmInline
//    value class Encoding(val task: Task?){
//        inline fun isExcluded(){
//            task?.run{ include = isNotInclude }
//        }
//        inline fun isOptional(){
//            task?.run{ include = isOptional }
//        }
//        inline fun setResolver(noinline block:VO.()->Boolean){
//            task?.run{ include = block }
//        }
//    }
//    inline val VO.encoding: Encoding get() = Encoding(_task)
////    inline fun Data.validator(vali:eVali){
////        _task?.run{ this.vali = vali }
////    }
//    inline fun VO.get(noinline block:(VO, Any)->Any?){
//        _task?.let{ task ->
//            (task.getTasks ?: arrayListOf<(VO, Any)->Any?>().also{task.getTasks = it}).add(block)
//        }
//    }
//    inline fun VO.set(noinline block:(VO, Any)->Any?){
//        _task?.let{ task ->
//            (task.setTasks ?: arrayListOf<(VO, Any)->Any?>().also{task.setTasks = it}).add(block)
//        }
//    }
// 코딩시 주의사항:
//  필드는 같은 타입에 대해 싱글턴으로 단 하나만 생기므로 필드안에 개별 객체 수준의 정보나 클래스 수준의 정보가
//  들어가면 안된다. 객체 수준 정보는 엔티티 객체를 참조하고, 클래스 수준의 정보는 클래스 스토어를 참조하는 식으로 처리해야 한다.
interface Field<VALUE:Any>
object IntField: Field<Int>
object UIntField: Field<UInt>
object LongField: Field<Long>
object ULongField: Field<ULong>
object ShortField: Field<Short>
object UShortField: Field<UShort>
object FloatField: Field<Float>
object DoubleField: Field<Double>
object BooleanField: Field<Boolean>
object StringField: Field<String>
//object UtcField: Field<eUtc>() {
//    @Suppress("NOTHING_TO_INLINE")
//    inline fun Data.default(noinline factory:(Data)->eUtc){
//        _defaultMap = _defaultMap ?: hashMapOf()
//        _defaultMap!![_index] = DefaultFactoryTask(factory)
//    }
//}
//
//object IntListField: Field<MutableList<Int>>(){
//    inline fun VO.default(noinline factory:(VO)->List<Int>){
//        _task?.default = factory
//    }
//}
//object UIntListField: Field<MutableList<UInt>>(){
//    inline fun VO.default(noinline factory:(VO)->List<UInt>){
//        _task?.default = factory
//    }
//}
//object LongListField: Field<MutableList<Long>>(){
//    inline fun VO.default(noinline factory:(VO)->List<Long>){
//        _task?.default = factory
//    }
//}
//object ULongListField: Field<MutableList<ULong>>(){
//    inline fun VO.default(noinline factory:(VO)->List<ULong>){
//        _task?.default = factory
//    }
//}
//object ShortListField: Field<MutableList<Short>>(){
//    inline fun VO.default(noinline factory:(VO)->List<Short>){
//        _task?.default = factory
//    }
//}
//object UShortListField: Field<MutableList<UShort>>(){
//    inline fun VO.default(noinline factory:(VO)->List<UShort>){
//        _task?.default = factory
//    }
//}
//object FloatListField: Field<MutableList<Float>>(){
//    inline fun VO.default(noinline factory:(VO)->List<Float>){
//        _task?.default = factory
//    }
//}
//object DoubleListField: Field<MutableList<Double>>(){
//    inline fun VO.default(noinline factory:(VO)->List<Double>){
//        _task?.default = factory
//    }
//}
//object BooleanListField: Field<MutableList<Boolean>>(){
//    inline fun VO.default(noinline factory:(VO)->List<Boolean>){
//        _task?.default = factory
//    }
//}
//object StringListField: Field<MutableList<String>>(){
//    inline fun VO.default(noinline factory:(VO)->List<String>){
//        _task?.default = factory
//    }
//}
//
//object IntMapField: Field<HashMap<String, Int>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, Int>){
//        _task?.default = factory
//    }
//}
//object UIntMapField: Field<HashMap<String, UInt>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, UInt>){
//        _task?.default = factory
//    }
//}
//object LongMapField: Field<HashMap<String, Long>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, Long>){
//        _task?.default = factory
//    }
//}
//object ULongMapField: Field<HashMap<String, ULong>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, ULong>){
//        _task?.default = factory
//    }
//}
//object ShortMapField: Field<HashMap<String, Short>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, Short>){
//        _task?.default = factory
//    }
//}
//object UShortMapField: Field<HashMap<String, UShort>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, UShort>){
//        _task?.default = factory
//    }
//}
//object FloatMapField: Field<HashMap<String, Float>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, Float>){
//        _task?.default = factory
//    }
//}
//object DoubleMapField: Field<HashMap<String, Double>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, Double>){
//        _task?.default = factory
//    }
//}
//object BooleanMapField: Field<HashMap<String, Boolean>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, Boolean>){
//        _task?.default = factory
//    }
//}
//object StringMapField: Field<HashMap<String, String>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, String>){
//        _task?.default = factory
//    }
//}