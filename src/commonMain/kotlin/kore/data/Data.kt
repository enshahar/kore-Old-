@file:Suppress("NOTHING_TO_INLINE", "PropertyName", "UNCHECKED_CAST")

package kore.data

import kore.error.E
import kore.data.field.*
import kore.data.indexer.Indexer
import kore.data.task.TaskStore
import kore.data.task.Task
import kotlin.jvm.JvmInline
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

abstract class Data:ReadWriteProperty<Data, Any>{
    class NoIndex(val name:String):E(name)
    class NotInitialized(val name:String):E(name)
    class GetTaskFail(val result:Any):E(result)
    class SetTaskFail(val result:Any):E(result)
    class DefaultNotValue(val value:Any):E(value)

    @JvmInline
    value class Immutable<T:Any>(val value:T)
    /** lazy 필드 매칭용 인덱서 */
    @PublishedApi internal var _index = 0
    /** 실제 값을 보관하는 저장소 */
    @PublishedApi internal var _values:MutableMap<String, Any?>? = null
    /** 외부에 표출되는 저장소 */
    val props:MutableMap<String, Any?> get() = _values ?: hashMapOf<String, Any?>().also{ _values = it }
    override fun getValue(thisRef: Data, property:KProperty<*>):Any{
        val type: KClass<out Data> = this::class
        val name: String = property.name
        val index: Int = Indexer.get(type, name)() ?: NoIndex(name).terminate()
        val task:Task? = (this as? SlowData)?._tasks?.get(index) ?: TaskStore(type, index)
        val result:Any = _values!![name] ?: task?.getDefault(this)?.let{
            setValue(thisRef, property, it)
            _values!![name]
        } ?: NotInitialized(name).terminate()
        return task?.getTasks?.fold(result){acc, getTask->
            getTask(this, acc) ?: GetTaskFail(acc).terminate()
        } ?: result
    }
    override fun setValue(thisRef: Data, property:KProperty<*>, value:Any){
        setRawValue(property.name, value)
    }
    fun setRawValue(name:String, value:Any){
        val type: KClass<out Data> = this::class
        val index: Int = Indexer.get(type, name)() ?: NoIndex(name).terminate()
        val task:Task? = (this as? SlowData)?._tasks?.get(index) ?: TaskStore(type, index)
        props[name] = task?.setTasks?.fold(value){acc, setTask->
            setTask(this, acc) ?: SetTaskFail(acc).terminate()
        } ?: value
    }
    @PublishedApi internal var _lastIndex = -1
    @PublishedApi internal var _task: Task? = null
    @Suppress("NOTHING_TO_INLINE")
    inline fun <FIELD: Field<*>> FIELD.firstTask():FIELD?{
        val slowData:SlowData? = this as? SlowData
        return if(slowData != null) {
            if(_index in slowData._tasks) null
            else{
                slowData._tasks[_index] = Task()
                this
            }
        }else TaskStore.firstTask(this@Data)?.let{
            /** 최초 생성된 Task라면 _task에 캐쉬를 잡고 필드 반환*/
            if(_task == null || _index != _lastIndex){
                _lastIndex = _index
                _task = it
            }
            this
        } ?: _task?.let{
            /** 캐쉬에 잡힌 _task가 있으면 그걸 반환*/
            this
        }
    }
    inline val int get() = int()
    inline val uint get() = uint()
    inline val long get() = long()
    inline val ulong get() = ulong()
    inline val short get() = short()
    inline val ushort get() = ushort()
    inline val float get() = float()
    inline val double get() = double()
    inline val boolean get() = boolean()
    inline val string get() = string()
//    val utc get() = utc()

    inline val intList get() = intList()
    inline val uintList get() = uintList()
    inline val longList get() = longList()
    inline val ulongList get() = ulongList()
    inline val shortList get() = shortList()
    inline val ushortList get() = ushortList()
    inline val floatList get() = floatList()
    inline val doubleList get() = doubleList()
    inline val booleanList get() = booleanList()
    inline val stringList get() = stringList()

    inline val intMap get() = intMap()
    inline val uintMap get() = uintMap()
    inline val longMap get() = longMap()
    inline val ulongMap get() = ulongMap()
    inline val shortMap get() = shortMap()
    inline val ushortMap get() = ushortMap()
    inline val floatMap get() = floatMap()
    inline val doubleMap get() = doubleMap()
    inline val booleanMap get() = booleanMap()
    inline val stringMap get() = stringMap()


    inline fun int(block: IntField.()->Unit = {}): Prop<Int> {
        IntField.firstTask()?.block()
        return IntField.delegator
    }
    inline fun uint(block: UIntField.()->Unit = {}): Prop<UInt> {
        UIntField.firstTask()?.block()
        return UIntField.delegator
    }
    inline fun long(block: LongField.()->Unit = {}): Prop<Long> {
        LongField.firstTask()?.block()
        return LongField.delegator
    }
    inline fun ulong(block: ULongField.()->Unit = {}): Prop<ULong> {
        ULongField.firstTask()?.block()
        return ULongField.delegator
    }
    inline fun short(block: ShortField.()->Unit = {}): Prop<Short> {
        ShortField.firstTask()?.block()
        return ShortField.delegator
    }
    inline fun ushort(block: UShortField.()->Unit = {}): Prop<UShort> {
        UShortField.firstTask()?.block()
        return UShortField.delegator
    }
    inline fun float(block: FloatField.()->Unit = {}): Prop<Float> {
        FloatField.firstTask()?.block()
        return FloatField.delegator
    }
    inline fun double(block: DoubleField.()->Unit = {}): Prop<Double> {
        DoubleField.firstTask()?.block()
        return DoubleField.delegator
    }
    inline fun boolean(block: BooleanField.()->Unit = {}): Prop<Boolean> {
        BooleanField.firstTask()?.block()
        return BooleanField.delegator
    }
    inline fun string(block: StringField.()->Unit = {}): Prop<String> {
        StringField.firstTask()?.block()
        return StringField.delegator
    }
//    inline fun utc(block:UtcField.()->Unit = {}):Prop<eUtc>{
//        UtcField.firstTask()?.block()
//        return UtcField.delegator
//    }
    inline fun intList(block: IntListField.()->Unit = {}): Prop<MutableList<Int>> {
        IntListField.firstTask()?.block()
        return IntListField.delegator
    }
    inline fun uintList(block: UIntListField.()->Unit = {}): Prop<MutableList<UInt>> {
        UIntListField.firstTask()?.block()
        return UIntListField.delegator
    }
    inline fun longList(block: LongListField.()->Unit = {}): Prop<MutableList<Long>> {
        LongListField.firstTask()?.block()
        return LongListField.delegator
    }
    inline fun ulongList(block: ULongListField.()->Unit = {}): Prop<MutableList<ULong>> {
        ULongListField.firstTask()?.block()
        return ULongListField.delegator
    }
    inline fun shortList(block: ShortListField.()->Unit = {}): Prop<MutableList<Short>> {
        ShortListField.firstTask()?.block()
        return ShortListField.delegator
    }
    inline fun ushortList(block: UShortListField.()->Unit = {}): Prop<MutableList<UShort>> {
        UShortListField.firstTask()?.block()
        return UShortListField.delegator
    }
    inline fun floatList(block: FloatListField.()->Unit = {}): Prop<MutableList<Float>> {
        FloatListField.firstTask()?.block()
        return FloatListField.delegator
    }
    inline fun doubleList(block: DoubleListField.()->Unit = {}): Prop<MutableList<Double>> {
        DoubleListField.firstTask()?.block()
        return DoubleListField.delegator
    }
    inline fun booleanList(block: BooleanListField.()->Unit = {}): Prop<MutableList<Boolean>> {
        BooleanListField.firstTask()?.block()
        return BooleanListField.delegator
    }
    inline fun stringList(block: StringListField.()->Unit = {}): Prop<MutableList<String>> {
        StringListField.firstTask()?.block()
        return StringListField.delegator
    }
    inline fun intMap(block: IntMapField.()->Unit = {}): Prop<HashMap<String, Int>> {
        IntMapField.firstTask()?.block()
        return IntMapField.delegator
    }
    inline fun uintMap(block: UIntMapField.()->Unit = {}): Prop<HashMap<String, UInt>> {
        UIntMapField.firstTask()?.block()
        return UIntMapField.delegator
    }
    inline fun longMap(block: LongMapField.()->Unit = {}): Prop<HashMap<String, Long>> {
        LongMapField.firstTask()?.block()
        return LongMapField.delegator
    }
    inline fun ulongMap(block: ULongMapField.()->Unit = {}): Prop<HashMap<String, ULong>> {
        ULongMapField.firstTask()?.block()
        return ULongMapField.delegator
    }
    inline fun shortMap(block: ShortMapField.()->Unit = {}): Prop<HashMap<String, Short>> {
        ShortMapField.firstTask()?.block()
        return ShortMapField.delegator
    }
    inline fun ushortMap(block: UShortMapField.()->Unit = {}): Prop<HashMap<String, UShort>> {
        UShortMapField.firstTask()?.block()
        return UShortMapField.delegator
    }
    inline fun floatMap(block: FloatMapField.()->Unit = {}): Prop<HashMap<String, Float>> {
        FloatMapField.firstTask()?.block()
        return FloatMapField.delegator
    }
    inline fun doubleMap(block: DoubleMapField.()->Unit = {}): Prop<HashMap<String, Double>> {
        DoubleMapField.firstTask()?.block()
        return DoubleMapField.delegator
    }
    inline fun booleanMap(block: BooleanMapField.()->Unit = {}): Prop<HashMap<String, Boolean>> {
        BooleanMapField.firstTask()?.block()
        return BooleanMapField.delegator
    }
    inline fun stringMap(block: StringMapField.()->Unit = {}): Prop<HashMap<String, String>> {
        StringMapField.firstTask()?.block()
        return StringMapField.delegator
    }
    val enums = hashMapOf<KClass<*>, Array<*>>()
    inline fun <reified ENUM:Enum<ENUM>> enum(block: EnumField<ENUM>.()->Unit = {}): Prop<ENUM> {
        if(ENUM::class !in enums) enums[ENUM::class] = enumValues<ENUM>()
        EnumField<ENUM>().firstTask()?.block()
        return EnumField<ENUM>().delegator
    }
    inline fun <reified ENUM:Enum<ENUM>> enumList(block: EnumListField<ENUM>.()->Unit = {}): Prop<MutableList<ENUM>> {
        if(ENUM::class !in enums) enums[ENUM::class] = enumValues<ENUM>()
        EnumListField<ENUM>().firstTask()?.block()
        return EnumListField<ENUM>().delegator
    }
    inline fun <reified ENUM:Enum<ENUM>> enumMap(block: EnumMapField<ENUM>.()->Unit = {}): Prop<MutableMap<String, ENUM>> {
        if(ENUM::class !in enums) enums[ENUM::class] = enumValues<ENUM>()
        EnumMapField<ENUM>().firstTask()?.block()
        return EnumMapField<ENUM>().delegator
    }
    inline fun <reified DATA: Data> entity(noinline factory:()->DATA, block: DataField<DATA>.()->Unit = {}): Prop<DATA> {
        DataField[factory].firstTask()?.block()
        return DataField[factory].delegator
    }
    inline fun <reified DATA: Data> entityList(noinline factory:()->DATA, block: DataListField<DATA>.()->Unit = {}): Prop<MutableList<DATA>> {
        DataListField[factory].firstTask()?.block()
        return DataListField[factory].delegator
    }
    inline fun <reified DATA: Data> entityMap(noinline factory:()->DATA, block: DataMapField<DATA>.()->Unit = {}): Prop<MutableMap<String, DATA>> {
        DataMapField[factory].firstTask()?.block()
        return DataMapField[factory].delegator
    }
    inline fun <DATA: Data> entityMap(cls:KClass<DATA>, noinline factory:()->DATA, block: DataMapField<DATA>.()->Unit = {}): Prop<MutableMap<String, DATA>> {
        DataMapField[cls, factory].firstTask()?.block()
        return DataMapField[cls, factory].delegator
    }
    inline fun <reified DATA: Data> union(union: Union<DATA>, block: UnionField<DATA>.()->Unit = {}): Prop<DATA> {
        UnionField[union].firstTask()?.block()
        return UnionField[union].delegator
    }
    inline fun <reified DATA: Data> unionList(union: Union<DATA>, block: UnionListField<DATA>.()->Unit = {}): Prop<MutableList<DATA>> {
        UnionListField[union].firstTask()?.block()
        return UnionListField[union].delegator
    }
    inline fun <reified DATA: Data> unionMap(union: Union<DATA>, block: UnionMapField<DATA>.()->Unit = {}): Prop<MutableMap<String, DATA>> {
        UnionMapField[union].firstTask()?.block()
        return UnionMapField[union].delegator
    }

    inline fun int(v:Int, block: IntField.()->Unit = {}): Prop<Int> {
        IntField.firstTask()?.apply{
            block()
            default(v)
        }
        return IntField.delegator
    }
    inline fun uint(v:UInt, block: UIntField.()->Unit = {}): Prop<UInt> {
        UIntField.firstTask()?.apply{
            block()
            default(v)
        }
        return UIntField.delegator
    }
    inline fun long(v:Long, block: LongField.()->Unit = {}): Prop<Long> {
        LongField.firstTask()?.apply{
            block()
            default(v)
        }
        return LongField.delegator
    }
    inline fun ulong(v:ULong, block: ULongField.()->Unit = {}): Prop<ULong> {
        ULongField.firstTask()?.apply{
            block()
            default(v)
        }
        return ULongField.delegator
    }
    inline fun short(v:Short, block: ShortField.()->Unit = {}): Prop<Short> {
        ShortField.firstTask()?.apply{
            block()
            default(v)
        }
        return ShortField.delegator
    }
    inline fun ushort(v:UShort, block: UShortField.()->Unit = {}): Prop<UShort> {
        UShortField.firstTask()?.apply{
            block()
            default(v)
        }
        return UShortField.delegator
    }
    inline fun float(v:Float, block: FloatField.()->Unit = {}): Prop<Float> {
        FloatField.firstTask()?.apply{
            block()
            default(v)
        }
        return FloatField.delegator
    }
    inline fun double(v:Double, block: DoubleField.()->Unit = {}): Prop<Double> {
        DoubleField.firstTask()?.apply{
            block()
            default(v)
        }
        return DoubleField.delegator
    }
    inline fun boolean(v:Boolean, block: BooleanField.()->Unit = {}): Prop<Boolean> {
        BooleanField.firstTask()?.apply{
            block()
            default(v)
        }
        return BooleanField.delegator
    }
    inline fun string(v:String, block: StringField.()->Unit = {}): Prop<String> {
        StringField.firstTask()?.apply{
            block()
            default(v)
        }
        return StringField.delegator
    }
    inline fun intList(vararg items:Int, block: IntListField.()->Unit = {}): Prop<MutableList<Int>> {
        IntListField.firstTask()?.apply{
            block()
            default{ArrayList<Int>(items.size).also{arr->items.forEach{arr.add(it)}}}
        }
        return IntListField.delegator
    }
    @OptIn(ExperimentalUnsignedTypes::class)
    inline fun uintList(vararg items:UInt, block: UIntListField.()->Unit = {}): Prop<MutableList<UInt>> {
        UIntListField.firstTask()?.apply{
            block()
            default{ArrayList<UInt>(items.size).also{it.addAll(items)}}
        }
        return UIntListField.delegator
    }
    inline fun longList(vararg items:Long, block: LongListField.()->Unit = {}): Prop<MutableList<Long>> {
        LongListField.firstTask()?.apply{
            block()
            default{ArrayList<Long>(items.size).also{arr->items.forEach{arr.add(it)}}}
        }
        return LongListField.delegator
    }
    @OptIn(ExperimentalUnsignedTypes::class)
    inline fun ulongList(vararg items:ULong, block: ULongListField.()->Unit = {}): Prop<MutableList<ULong>> {
        ULongListField.firstTask()?.apply{
            block()
            default{ArrayList<ULong>(items.size).also{it.addAll(items)}}
        }
        return ULongListField.delegator
    }
    inline fun shortList(vararg items:Short, block: ShortListField.()->Unit = {}): Prop<MutableList<Short>> {
        ShortListField.firstTask()?.apply{
            block()
            default{ArrayList<Short>(items.size).also{arr->items.forEach{arr.add(it)}}}
        }
        return ShortListField.delegator
    }
    @OptIn(ExperimentalUnsignedTypes::class)
    inline fun ushortList(vararg items:UShort, block: UShortListField.()->Unit = {}): Prop<MutableList<UShort>> {
        UShortListField.firstTask()?.apply{
            block()
            default{ArrayList<UShort>(items.size).also{it.addAll(items)}}
        }
        return UShortListField.delegator
    }
    inline fun floatList(vararg items:Float, block: FloatListField.()->Unit = {}): Prop<MutableList<Float>> {
        FloatListField.firstTask()?.apply{
            block()
            default{ArrayList<Float>(items.size).also{arr->items.forEach{arr.add(it)}}}
        }
        return FloatListField.delegator
    }
    inline fun doubleList(vararg items:Double, block: DoubleListField.()->Unit = {}): Prop<MutableList<Double>> {
        DoubleListField.firstTask()?.apply{
            block()
            default{ArrayList<Double>(items.size).also{arr->items.forEach{arr.add(it)}}}
        }
        return DoubleListField.delegator
    }
    inline fun booleanList(vararg items:Boolean, block: BooleanListField.()->Unit = {}): Prop<MutableList<Boolean>> {
        BooleanListField.firstTask()?.apply{
            block()
            default{ArrayList<Boolean>(items.size).also{arr->items.forEach{arr.add(it)}}}
        }
        return BooleanListField.delegator
    }
    inline fun stringList(vararg items:String, block: StringListField.()->Unit = {}): Prop<MutableList<String>> {
        StringListField.firstTask()?.apply{
            block()
            default{ArrayList<String>(items.size).also{it.addAll(items)}}
        }
        return StringListField.delegator
    }
    inline fun intMap(vararg items:Pair<String, Int>, block: IntMapField.()->Unit = {}): Prop<HashMap<String, Int>> {
        IntMapField.firstTask()?.apply{
            block()
            default{HashMap<String, Int>(items.size).also{it.putAll(items)}}
        }
        return IntMapField.delegator
    }
    inline fun uintMap(vararg items:Pair<String, UInt>, block: UIntMapField.()->Unit = {}): Prop<HashMap<String, UInt>> {
        UIntMapField.firstTask()?.apply{
            block()
            default{HashMap<String, UInt>(items.size).also{it.putAll(items)}}
        }
        return UIntMapField.delegator
    }
    inline fun longMap(vararg items:Pair<String, Long>, block: LongMapField.()->Unit = {}): Prop<HashMap<String, Long>> {
        LongMapField.firstTask()?.apply{
            block()
            default{HashMap<String, Long>(items.size).also{it.putAll(items)}}
        }
        return LongMapField.delegator
    }
    inline fun ulongMap(vararg items:Pair<String, ULong>, block: ULongMapField.()->Unit = {}): Prop<HashMap<String, ULong>> {
        ULongMapField.firstTask()?.apply{
            block()
            default{HashMap<String, ULong>(items.size).also{it.putAll(items)}}
        }
        return ULongMapField.delegator
    }
    inline fun shortMap(vararg items:Pair<String, Short>, block: ShortMapField.()->Unit = {}): Prop<HashMap<String, Short>> {
        ShortMapField.firstTask()?.apply{
            block()
            default{HashMap<String, Short>(items.size).also{it.putAll(items)}}
        }
        return ShortMapField.delegator
    }
    inline fun ushortMap(vararg items:Pair<String, UShort>, block: UShortMapField.()->Unit = {}): Prop<HashMap<String, UShort>> {
        UShortMapField.firstTask()?.apply{
            block()
            default{HashMap<String, UShort>(items.size).also{it.putAll(items)}}
        }
        return UShortMapField.delegator
    }
    inline fun floatMap(vararg items:Pair<String, Float>, block: FloatMapField.()->Unit = {}): Prop<HashMap<String, Float>> {
        FloatMapField.firstTask()?.apply{
            block()
            default{HashMap<String, Float>(items.size).also{it.putAll(items)}}
        }
        return FloatMapField.delegator
    }
    inline fun doubleMap(vararg items:Pair<String, Double>, block: DoubleMapField.()->Unit = {}): Prop<HashMap<String, Double>> {
        DoubleMapField.firstTask()?.apply{
            block()
            default{HashMap<String, Double>(items.size).also{it.putAll(items)}}
        }
        return DoubleMapField.delegator
    }
    inline fun booleanMap(vararg items:Pair<String, Boolean>, block: BooleanMapField.()->Unit = {}): Prop<HashMap<String, Boolean>> {
        BooleanMapField.firstTask()?.apply{
            block()
            default{HashMap<String, Boolean>(items.size).also{it.putAll(items)}}
        }
        return BooleanMapField.delegator
    }
    inline fun stringMap(vararg items:Pair<String, String>, block: StringMapField.()->Unit = {}): Prop<HashMap<String, String>> {
        StringMapField.firstTask()?.apply{
            block()
            default{HashMap<String, String>(items.size).also{it.putAll(items)}}
        }
        return StringMapField.delegator
    }
    inline fun <reified ENUM:Enum<ENUM>> enum(v:ENUM, block: EnumField<ENUM>.()->Unit = {}): Prop<ENUM> {
        EnumField<ENUM>().firstTask()?.apply{
            block()
            default(v)
        }
        return EnumField<ENUM>().delegator
    }
    inline fun <reified ENUM:Enum<ENUM>> enumList(vararg items:ENUM, block: EnumListField<ENUM>.()->Unit = {}): Prop<MutableList<ENUM>> {
        EnumListField<ENUM>().firstTask()?.apply{
            block()
            default{ArrayList<ENUM>(items.size).also{it.addAll(items)}}
        }
        return EnumListField<ENUM>().delegator
    }
    inline fun <reified ENUM:Enum<ENUM>> enumMap(vararg items:Pair<String, ENUM>, block: EnumMapField<ENUM>.()->Unit = {}): Prop<MutableMap<String, ENUM>> {
        EnumMapField<ENUM>().firstTask()?.apply{
            block()
            default{HashMap<String, ENUM>(items.size).also{it.putAll(items)}}
        }
        return EnumMapField<ENUM>().delegator
    }
}
