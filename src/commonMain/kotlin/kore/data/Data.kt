@file:Suppress("NOTHING_TO_INLINE")

package kore.data

import kore.error.E
import ein2b.core.entity.field.*
import kore.data.field.*
import kore.data.task.TaskStore
import kore.data.task.Task
import kotlin.jvm.JvmInline
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

abstract class Data:ReadWriteProperty<Data, Any>{
    class NoIndex(name:String):E(name)
    class NotInitialized(name:String):E(name)
    class GetTaskFail(result:Any):E(result)
    class SetTaskFail(result:Any):E(result)
    class DefaultNotValue(value:Any):E(value)
//        setValue_ruleFail,
//        setValue_taskFail,
//        encode_error,
//        decode_error,
//        index_error,
    @JvmInline
    value class Immutable<T:Any>(val value:T)
    companion object{
//        inline fun <T: Data>parse(entity:T, json:String, report: Report = Report(), error:((Report)->Unit) = {}):T?{
//            val r = entity.unserializeJson(json){
//                report.id = it.id
//                report.message = it.message
//                report.result = it.result
//            }
//            return if(r != null && report.id == null) r
//            else{
//                error.invoke(report)
//                null
//            }
//        }
//        inline fun <T: Data>parseEin(entity:T, str:String, report: Report = Report(), error:((Report)->Unit) = {}):T?{
//            val r = entity.unserializeEin(str){
//                report.id = it.id
//                report.message = it.message
//                report.result = it.result
//            }
//            return if(r != null && report.id == null) r
//            else{
//                error.invoke(report)
//                null
//            }
//        }
        object Default{
            val stringList:()->MutableList<String> = {arrayListOf()}
            val stringMap:()->HashMap<String,String> = {hashMapOf()}
        }
    }

    /** lazy 필드 매칭용 인덱서 */
    @PublishedApi internal var _index = 0
    /** 실제 값을 보관하는 저장소 */
    @PublishedApi internal var _values:MutableMap<String, Any>? = null
    /** 외부에 표출되는 저장소 */
    val props:MutableMap<String, Any> get() = _values ?: hashMapOf<String, Any>().also{ _values = it }
    override fun getValue(thisRef: Data, property:KProperty<*>):Any{
        val name: String = property.name
        val result:Any = _values!![name] ?: TaskStore.default(this, name)?.let{
            setValue(thisRef, property, it)
            _values!![property.name]
        } ?: NotInitialized(name).terminate()
        return TaskStore.getTask(this::class, name)?.fold(result){acc, task->
            task(acc) ?: GetTaskFail(acc).terminate()
        } ?: result
    }
    override fun setValue(thisRef: Data, property:KProperty<*>, value:Any){
        setRawValue(property.name, value)
    }
    fun setRawValue(name:String, value:Any){
        var newValue:Any = value
//        TaskStore.getVali(this, name)?.let{
//            val (isOk, result) = it.check(newValue)
//            if(!isOk){
//                throw E(
//                    ERROR.setValue_ruleFail,
//                    "rule fail. ${this::class.simpleName}.${name} value:0.$newValue, result:1.$result",
//                    newValue, result
//                )
//            }
//            newValue = result
//        }
        _values!![name] = TaskStore.setTask(this::class, name)?.fold(newValue){acc, task->
            task(acc) ?: SetTaskFail(acc).terminate()
        } ?: newValue
    }
    val int get() = int()
    val uint get() = uint()
    val long get() = long()
    val ulong get() = ulong()
    val short get() = short()
    val ushort get() = ushort()
    val float get() = float()
    val double get() = double()
    val boolean get() = boolean()
    val string get() = string()
//    val utc get() = utc()

    val intList get() = intList()
    val uintList get() = uintList()
    val longList get() = longList()
    val ulongList get() = ulongList()
    val shortList get() = shortList()
    val ushortList get() = ushortList()
    val floatList get() = floatList()
    val doubleList get() = doubleList()
    val booleanList get() = booleanList()
    val stringList get() = stringList()

    val intMap get() = intMap()
    val uintMap get() = uintMap()
    val longMap get() = longMap()
    val ulongMap get() = ulongMap()
    val shortMap get() = shortMap()
    val ushortMap get() = ushortMap()
    val floatMap get() = floatMap()
    val doubleMap get() = doubleMap()
    val booleanMap get() = booleanMap()
    val stringMap get() = stringMap()

    @PublishedApi internal var _lastIndex = -1
    @PublishedApi internal var _task: Task? = null
    @Suppress("NOTHING_TO_INLINE")
    inline fun <FIELD: Field<*>> FIELD.firstTask():FIELD?{
        return TaskStore.firstTask(this@Data)?.let{
            if(_task == null || _index != _lastIndex){
                _lastIndex = _index
                _task = it
            }
            this
        } ?: _task?.let{
            this
        }
    }
    inline fun int(block: IntField.()->Unit = {}):Prop<Int>{
        IntField.firstTask()?.block()
        return IntField.delegator
    }
    inline fun uint(block: UIntField.()->Unit = {}):Prop<UInt>{
        UIntField.firstTask()?.block()
        return UIntField.delegator
    }
    inline fun long(block: LongField.()->Unit = {}):Prop<Long>{
        LongField.firstTask()?.block()
        return LongField.delegator
    }
    inline fun ulong(block: ULongField.()->Unit = {}):Prop<ULong>{
        ULongField.firstTask()?.block()
        return ULongField.delegator
    }
    inline fun short(block: ShortField.()->Unit = {}):Prop<Short>{
        ShortField.firstTask()?.block()
        return ShortField.delegator
    }
    inline fun ushort(block: UShortField.()->Unit = {}):Prop<UShort>{
        UShortField.firstTask()?.block()
        return UShortField.delegator
    }
    inline fun float(block: FloatField.()->Unit = {}):Prop<Float>{
        FloatField.firstTask()?.block()
        return FloatField.delegator
    }
    inline fun double(block: DoubleField.()->Unit = {}):Prop<Double>{
        DoubleField.firstTask()?.block()
        return DoubleField.delegator
    }
    inline fun boolean(block: BooleanField.()->Unit = {}):Prop<Boolean>{
        BooleanField.firstTask()?.block()
        return BooleanField.delegator
    }
    inline fun string(block: StringField.()->Unit = {}):Prop<String>{
        StringField.firstTask()?.block()
        return StringField.delegator
    }
//    inline fun utc(block:UtcField.()->Unit = {}):Prop<eUtc>{
//        UtcField.firstTask()?.block()
//        return UtcField.delegator
//    }
    inline fun intList(block: IntListField.()->Unit = {}):Prop<MutableList<Int>>{
        IntListField.firstTask()?.block()
        return IntListField.delegator
    }
    inline fun uintList(block: UIntListField.()->Unit = {}):Prop<MutableList<UInt>>{
        UIntListField.firstTask()?.block()
        return UIntListField.delegator
    }
    inline fun longList(block: LongListField.()->Unit = {}):Prop<MutableList<Long>>{
        LongListField.firstTask()?.block()
        return LongListField.delegator
    }
    inline fun ulongList(block: ULongListField.()->Unit = {}):Prop<MutableList<ULong>>{
        ULongListField.firstTask()?.block()
        return ULongListField.delegator
    }
    inline fun shortList(block: ShortListField.()->Unit = {}):Prop<MutableList<Short>>{
        ShortListField.firstTask()?.block()
        return ShortListField.delegator
    }
    inline fun ushortList(block: UShortListField.()->Unit = {}):Prop<MutableList<UShort>>{
        UShortListField.firstTask()?.block()
        return UShortListField.delegator
    }
    inline fun floatList(block: FloatListField.()->Unit = {}):Prop<MutableList<Float>>{
        FloatListField.firstTask()?.block()
        return FloatListField.delegator
    }
    inline fun doubleList(block: DoubleListField.()->Unit = {}):Prop<MutableList<Double>>{
        DoubleListField.firstTask()?.block()
        return DoubleListField.delegator
    }
    inline fun booleanList(block: BooleanListField.()->Unit = {}):Prop<MutableList<Boolean>>{
        BooleanListField.firstTask()?.block()
        return BooleanListField.delegator
    }
    inline fun stringList(block: StringListField.()->Unit = {}):Prop<MutableList<String>>{
        StringListField.firstTask()?.block()
        return StringListField.delegator
    }
    inline fun intMap(block: IntMapField.()->Unit = {}):Prop<HashMap<String, Int>>{
        IntMapField.firstTask()?.block()
        return IntMapField.delegator
    }
    inline fun uintMap(block: UIntMapField.()->Unit = {}):Prop<HashMap<String, UInt>>{
        UIntMapField.firstTask()?.block()
        return UIntMapField.delegator
    }
    inline fun longMap(block: LongMapField.()->Unit = {}):Prop<HashMap<String, Long>>{
        LongMapField.firstTask()?.block()
        return LongMapField.delegator
    }
    inline fun ulongMap(block: ULongMapField.()->Unit = {}):Prop<HashMap<String, ULong>>{
        ULongMapField.firstTask()?.block()
        return ULongMapField.delegator
    }
    inline fun shortMap(block: ShortMapField.()->Unit = {}):Prop<HashMap<String, Short>>{
        ShortMapField.firstTask()?.block()
        return ShortMapField.delegator
    }
    inline fun ushortMap(block: UShortMapField.()->Unit = {}):Prop<HashMap<String, UShort>>{
        UShortMapField.firstTask()?.block()
        return UShortMapField.delegator
    }
    inline fun floatMap(block: FloatMapField.()->Unit = {}):Prop<HashMap<String, Float>>{
        FloatMapField.firstTask()?.block()
        return FloatMapField.delegator
    }
    inline fun doubleMap(block: DoubleMapField.()->Unit = {}):Prop<HashMap<String, Double>>{
        DoubleMapField.firstTask()?.block()
        return DoubleMapField.delegator
    }
    inline fun booleanMap(block: BooleanMapField.()->Unit = {}):Prop<HashMap<String, Boolean>>{
        BooleanMapField.firstTask()?.block()
        return BooleanMapField.delegator
    }
    inline fun stringMap(block: StringMapField.()->Unit = {}):Prop<HashMap<String, String>>{
        StringMapField.firstTask()?.block()
        return StringMapField.delegator
    }
    val enums = hashMapOf<KClass<*>, Array<*>>()
    inline fun <reified ENUM:Enum<ENUM>> enum(block:EnumField<ENUM>.()->Unit = {}):Prop<ENUM>{
        if(ENUM::class !in enums) enums[ENUM::class] = enumValues<ENUM>()
        @Suppress("UNCHECKED_CAST")
        EnumField.get(ENUM::class, enums[ENUM::class] as Array<ENUM>).firstTask()?.block()
        return EnumField.get(ENUM::class, null).delegator
    }
    inline fun <reified ENUM:Enum<ENUM>> enumList(block:EnumListField<ENUM>.()->Unit = {}):Prop<MutableList<ENUM>>{
        if(ENUM::class !in enums) enums[ENUM::class] = enumValues<ENUM>()
        @Suppress("UNCHECKED_CAST")
        EnumListField.get(ENUM::class, enums[ENUM::class] as Array<ENUM>).firstTask()?.block()
        return EnumListField.get(ENUM::class, null).delegator
    }
    inline fun <reified ENUM:Enum<ENUM>> enumMap(block:EnumMapField<ENUM>.()->Unit = {}):Prop<MutableMap<String, ENUM>>{
        if(ENUM::class !in enums) enums[ENUM::class] = enumValues<ENUM>()
        @Suppress("UNCHECKED_CAST")
        EnumMapField.get(ENUM::class, enums[ENUM::class] as Array<ENUM>).firstTask()?.block()
        return EnumMapField.get(ENUM::class, null).delegator
    }
    inline fun <reified ENTITY: Data> entity(noinline factory:()->ENTITY, block:EntityField<ENTITY>.()->Unit = {}):Prop<ENTITY>{
//        println("Adding : this=${this::class.qualifiedName}")
        EntityField[factory].firstTask()?.block()
        return EntityField[factory].delegator
    }
    inline fun <reified ENTITY: Data> entityList(noinline factory:()->ENTITY, block:EntityListField<ENTITY>.()->Unit = {}):Prop<MutableList<ENTITY>>{
        EntityListField[factory].firstTask()?.block()
        return EntityListField[factory].delegator
    }
    inline fun <reified ENTITY: Data> entityMap(noinline factory:()->ENTITY, block:EntityMapField<ENTITY>.()->Unit = {}):Prop<MutableMap<String, ENTITY>>{
        EntityMapField[factory].firstTask()?.block()
        return EntityMapField[factory].delegator
    }
    inline fun <reified ENTITY: Data> union(union: Union<ENTITY>, block:UnionField<ENTITY>.()->Unit = {}):Prop<ENTITY>{
        UnionField[union].firstTask()?.block()
        return UnionField[union].delegator
    }
    inline fun <reified ENTITY: Data> unionList(union: Union<ENTITY>, block:UnionListField<ENTITY>.()->Unit = {}):Prop<MutableList<ENTITY>>{
        UnionListField[union].firstTask()?.block()
        return UnionListField[union].delegator
    }
    inline fun <reified ENTITY: Data> unionMap(union: Union<ENTITY>, block:UnionMapField<ENTITY>.()->Unit = {}):Prop<MutableMap<String, ENTITY>>{
        UnionMapField[union].firstTask()?.block()
        return UnionMapField[union].delegator
    }

    inline fun int(v:Int, block: IntField.()->Unit = {}):Prop<Int>{
        IntField.firstTask()?.apply{
            block()
            default(v)
        }
        return IntField.delegator
    }
    inline fun uint(v:UInt, block: UIntField.()->Unit = {}):Prop<UInt>{
        UIntField.firstTask()?.apply{
            block()
            default(v)
        }
        return UIntField.delegator
    }
    inline fun long(v:Long, block: LongField.()->Unit = {}):Prop<Long>{
        LongField.firstTask()?.apply{
            block()
            default(v)
        }
        return LongField.delegator
    }
    inline fun ulong(v:ULong, block: ULongField.()->Unit = {}):Prop<ULong>{
        ULongField.firstTask()?.apply{
            block()
            default(v)
        }
        return ULongField.delegator
    }
    inline fun short(v:Short, block: ShortField.()->Unit = {}):Prop<Short>{
        ShortField.firstTask()?.apply{
            block()
            default(v)
        }
        return ShortField.delegator
    }
    inline fun ushort(v:UShort, block: UShortField.()->Unit = {}):Prop<UShort>{
        UShortField.firstTask()?.apply{
            block()
            default(v)
        }
        return UShortField.delegator
    }
    inline fun float(v:Float, block: FloatField.()->Unit = {}):Prop<Float>{
        FloatField.firstTask()?.apply{
            block()
            default(v)
        }
        return FloatField.delegator
    }
    inline fun double(v:Double, block: DoubleField.()->Unit = {}):Prop<Double>{
        DoubleField.firstTask()?.apply{
            block()
            default(v)
        }
        return DoubleField.delegator
    }
    inline fun boolean(v:Boolean, block: BooleanField.()->Unit = {}):Prop<Boolean>{
        BooleanField.firstTask()?.apply{
            block()
            default(v)
        }
        return BooleanField.delegator
    }
    inline fun string(v:String, block: StringField.()->Unit = {}):Prop<String>{
        StringField.firstTask()?.apply{
            block()
            default(v)
        }
        return StringField.delegator
    }
    inline fun intList(vararg items:Int, block: IntListField.()->Unit = {}):Prop<MutableList<Int>>{
        IntListField.firstTask()?.apply{
            block()
            default(items::toMutableList)
        }
        return IntListField.delegator
    }
    @OptIn(ExperimentalUnsignedTypes::class)
    inline fun uintList(vararg items:UInt, block: UIntListField.()->Unit = {}):Prop<MutableList<UInt>>{
        UIntListField.firstTask()?.apply{
            block()
            default(items::toMutableList)
        }
        return UIntListField.delegator
    }
    inline fun longList(vararg items:Long, block: LongListField.()->Unit = {}):Prop<MutableList<Long>>{
        LongListField.firstTask()?.apply{
            block()
            default(items::toMutableList)
        }
        return LongListField.delegator
    }
    @OptIn(ExperimentalUnsignedTypes::class)
    inline fun ulongList(vararg items:ULong, block: ULongListField.()->Unit = {}):Prop<MutableList<ULong>>{
        ULongListField.firstTask()?.apply{
            block()
            default(items::toMutableList)
        }
        return ULongListField.delegator
    }
    inline fun shortList(vararg items:Short, block: ShortListField.()->Unit = {}):Prop<MutableList<Short>>{
        ShortListField.firstTask()?.apply{
            block()
            default(items::toMutableList)
        }
        return ShortListField.delegator
    }
    @OptIn(ExperimentalUnsignedTypes::class)
    inline fun ushortList(vararg items:UShort, block: UShortListField.()->Unit = {}):Prop<MutableList<UShort>>{
        UShortListField.firstTask()?.apply{
            block()
            default(items::toMutableList)
        }
        return UShortListField.delegator
    }
    inline fun floatList(vararg items:Float, block: FloatListField.()->Unit = {}):Prop<MutableList<Float>>{
        FloatListField.firstTask()?.apply{
            block()
            default(items::toMutableList)
        }
        return FloatListField.delegator
    }
    inline fun doubleList(vararg items:Double, block: DoubleListField.()->Unit = {}):Prop<MutableList<Double>>{
        DoubleListField.firstTask()?.apply{
            block()
            default(items::toMutableList)
        }
        return DoubleListField.delegator
    }
    inline fun booleanList(vararg items:Boolean, block: BooleanListField.()->Unit = {}):Prop<MutableList<Boolean>>{
        BooleanListField.firstTask()?.apply{
            block()
            default(items::toMutableList)
        }
        return BooleanListField.delegator
    }
    inline fun stringList(vararg items:String, block: StringListField.()->Unit = {}):Prop<MutableList<String>>{
        StringListField.firstTask()?.apply{
            block()
            default(items::toMutableList)
        }
        return StringListField.delegator
    }
    inline fun intMap(vararg items:Pair<String, Int>, block: IntMapField.()->Unit = {}):Prop<HashMap<String, Int>>{
        IntMapField.firstTask()?.apply{
            block()
            default{
                items.fold(hashMapOf()) { map, it ->
                    map[it.first] = it.second
                    map
                }
            }
        }
        return IntMapField.delegator
    }
    inline fun uintMap(vararg items:Pair<String, UInt>, block: UIntMapField.()->Unit = {}):Prop<HashMap<String, UInt>>{
        UIntMapField.firstTask()?.apply{
            block()
            default{
                items.fold(hashMapOf()) { map, it ->
                    map[it.first] = it.second
                    map
                }
            }
        }
        return UIntMapField.delegator
    }
    inline fun longMap(vararg items:Pair<String, Long>, block: LongMapField.()->Unit = {}):Prop<HashMap<String, Long>>{
        LongMapField.firstTask()?.apply{
            block()
            default{
                items.fold(hashMapOf()) { map, it ->
                    map[it.first] = it.second
                    map
                }
            }
        }
        return LongMapField.delegator
    }
    inline fun ulongMap(vararg items:Pair<String, ULong>, block: ULongMapField.()->Unit = {}):Prop<HashMap<String, ULong>>{
        ULongMapField.firstTask()?.apply{
            block()
            default{
                items.fold(hashMapOf()) { map, it ->
                    map[it.first] = it.second
                    map
                }
            }
        }
        return ULongMapField.delegator
    }
    inline fun shortMap(vararg items:Pair<String, Short>, block: ShortMapField.()->Unit = {}):Prop<HashMap<String, Short>>{
        ShortMapField.firstTask()?.apply{
            block()
            default{
                items.fold(hashMapOf()) { map, it ->
                    map[it.first] = it.second
                    map
                }
            }
        }
        return ShortMapField.delegator
    }
    inline fun ushortMap(vararg items:Pair<String, UShort>, block: UShortMapField.()->Unit = {}):Prop<HashMap<String, UShort>>{
        UShortMapField.firstTask()?.apply{
            block()
            default{
                items.fold(hashMapOf()) { map, it ->
                    map[it.first] = it.second
                    map
                }
            }
        }
        return UShortMapField.delegator
    }
    inline fun floatMap(vararg items:Pair<String, Float>, block: FloatMapField.()->Unit = {}):Prop<HashMap<String, Float>>{
        FloatMapField.firstTask()?.apply{
            block()
            default{
                items.fold(hashMapOf()) { map, it ->
                    map[it.first] = it.second
                    map
                }
            }
        }
        return FloatMapField.delegator
    }
    inline fun doubleMap(vararg items:Pair<String, Double>, block: DoubleMapField.()->Unit = {}):Prop<HashMap<String, Double>>{
        DoubleMapField.firstTask()?.apply{
            block()
            default{
                items.fold(hashMapOf()) { map, it ->
                    map[it.first] = it.second
                    map
                }
            }
        }
        return DoubleMapField.delegator
    }
    inline fun booleanMap(vararg items:Pair<String, Boolean>, block: BooleanMapField.()->Unit = {}):Prop<HashMap<String, Boolean>>{
        BooleanMapField.firstTask()?.apply{
            block()
            default{
                items.fold(hashMapOf()) { map, it ->
                    map[it.first] = it.second
                    map
                }
            }
        }
        return BooleanMapField.delegator
    }
    inline fun stringMap(vararg items:Pair<String, String>, block: StringMapField.()->Unit = {}):Prop<HashMap<String, String>>{
        StringMapField.firstTask()?.apply{
            block()
            default{
                items.fold(hashMapOf()) { map, it ->
                    map[it.first] = it.second
                    map
                }
            }
        }
        return StringMapField.delegator
    }
    inline fun <reified ENUM:Enum<ENUM>> enum(enums:()->Array<ENUM>, v:ENUM, block:EnumField<ENUM>.()->Unit = {}):Prop<ENUM>{
        EnumField.get(ENUM::class, enums()).firstTask()?.apply{
            block()
            default(v)
        }
        return EnumField.get(ENUM::class, null).delegator
    }
    inline fun <reified ENUM:Enum<ENUM>> enumList(enums:()->Array<ENUM>, vararg items:ENUM, block:EnumListField<ENUM>.()->Unit = {}):Prop<MutableList<ENUM>>{
        EnumListField.get(ENUM::class, enums()).firstTask()?.apply{
            block()
            default(items::toMutableList)
        }
        return EnumListField.get(ENUM::class, null).delegator
    }
    inline fun <reified ENUM:Enum<ENUM>> enumMap(enums:()->Array<ENUM>, vararg items:Pair<String, ENUM>, block:EnumMapField<ENUM>.()->Unit = {}):Prop<MutableMap<String, ENUM>>{
        EnumMapField.get(ENUM::class, enums()).firstTask()?.apply{
            block()
            default{
                items.fold(hashMapOf()) { map, it ->
                    map[it.first] = it.second
                    map
                }
            }
        }
        return EnumMapField.get(ENUM::class, null).delegator
    }
}
abstract class SlowData:Data(){
    val _fields:HashMap<String, Field<*>> = hashMapOf()
    val _tasks:HashMap<Int, Task> = hashMapOf()
}
//abstract class eSlowEntity(isOrderedMap:Boolean = false): Data(isOrderedMap) {
//
//
//    inline fun <ENTITY: Data> entityMap(cls:KClass<ENTITY>, noinline factory:()->ENTITY, block:SlowEntityMapField<ENTITY>.()->Unit = {}):Prop<MutableMap<String, ENTITY>>{
//        SlowEntityMapField[cls,factory].firstTask()?.block()
//        return SlowEntityMapField[cls,factory].delegator
//    }
//    inline fun <ENTITY: Data> entity(cls:KClass<ENTITY>, noinline factory:()->ENTITY, block:SlowEntityField<ENTITY>.()->Unit = {}):Prop<ENTITY>{
////        println("field:${this} Adding : this=${this::class.qualifiedName}, cls=${cls.qualifiedName}")
//        SlowEntityField[cls, factory].firstTask()?.block()
//        return SlowEntityField[cls, factory].delegator
//    }
//    inline fun <ENTITY: Data> entityList(cls:KClass<ENTITY>, noinline factory:()->ENTITY, block:SlowEntityListField<ENTITY>.()->Unit = {}):Prop<MutableList<ENTITY>>{
//        SlowEntityListField[cls, factory].firstTask()?.block()
//        return SlowEntityListField[cls, factory].delegator
//    }
//}