@file:Suppress("NOTHING_TO_INLINE", "PropertyName", "UNCHECKED_CAST", "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
    "ObjectPropertyName"
)

package kore.data

import kore.data.field.*
import kore.data.task.Task
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

abstract class VO(useInstanceField:Boolean = false){ /** 인스턴스에서 필드 정보를 기록할지 여부 */
    companion object{
        /** 전역 태스크 저장소 */
        @PublishedApi internal val _voTasks:HashMap<KClass<out VO>, HashMap<String, Task>> = hashMapOf()
        /** 전역 필드 저장소 */
        @PublishedApi internal val _voFields:HashMap<KClass<out VO>, HashMap<String, Field<*>>> = hashMapOf()
        /** 전역 필드이름 및 순번 저장소 */
        @PublishedApi internal val _voKeys:HashMap<KClass<out VO>, ArrayList<String>> = hashMapOf()
        /** 파서 등에서 해당 VO의 필드이름 리스트를 얻음 */
        fun fields(type:KClass<out VO>):List<String>? = _voKeys[type]
        /** 모든 VO가 사용하는 델리게이트*/
        @PublishedApi internal val _delegate: ReadWriteProperty<VO, Any> = object:ReadWriteProperty<VO, Any>{
            override fun getValue(vo: VO, property: KProperty<*>): Any {
                val key:String = property.name
                val task:Task? = vo.getTask(key)
                val result:Any = vo.values[key] ?: task?.getDefault(vo, key) ?: Task.NoDefault(vo, key).terminate()
                return task?.getFold(vo, key, result) ?: result
            }
            override fun setValue(vo: VO, property: KProperty<*>, value: Any){ vo[property.name] = value }
        }
        /** 모든 VO가 사용하는 델리게이트 프로바이더*/
        @PublishedApi internal val _delegateProvider: PropertyDelegateProvider<VO, ReadWriteProperty<VO, Any>> = PropertyDelegateProvider{ vo, prop->
            val key: String = prop.name
            /** 타입별 필드 저장소 얻기(인스턴스 저장소 우선) */
            val fields:HashMap<String, Field<*>> = vo._fields ?: _voFields.getOrPut(vo.type){
                /** 전역 필드 초기화 시점이 전역 태스크 초기화 시점임*/
                _voTasks[vo.type] = hashMapOf()
                hashMapOf()
            }
            /**필드가 최초로 정의되는 경우*/
            if(key !in fields){
                /**필드이름 리스트에 추가*/
                vo.__keys__?.let{
                    it.add(key)
                    _voKeys[vo.type] = it
                }
                /**필드타입정보를 __field__로부터 복사. null이면 정상적인 절차가 아니므로 throw*/
                fields[key] = vo.__field__!!
                /**필드를 null로 초기화 함*/
                vo.values[key] = null
                /**필드용 태스크를 __task__로부터 선택적으로 복사(안올 수도 있음)*/
                vo.__task__?.let{
                    (vo._tasks ?: _voTasks[vo.type])?.put(key, it)
                }
            }
            /** 임시 전달용 변수 초기화 */
            vo.__task__ = null
            vo.__field__ = null
            _delegate
        }
    }
    /** 실제 값을 보관하는 저장소 */
    @PublishedApi internal var _values:MutableMap<String, Any?>? = null
    @PublishedApi internal inline val values:MutableMap<String, Any?> get() = _values ?: hashMapOf<String, Any?>().also{ _values = it }
    /** 속성 setter*/
    operator fun set(key:String, value:Any){
        values[key] = getTask(key)?.setFold(this, key, value) ?: value
    }
    /** 외부에 표출되는 저장소 */
    inline val props:Map<String, Any?> get() = values
    /** 인스턴스 필드 저장소를 쓸 경우 */
    @PublishedApi internal val _fields:HashMap<String, Field<*>>? = if(useInstanceField) hashMapOf() else null
    @PublishedApi internal val _tasks:HashMap<String, Task>? = if(useInstanceField) hashMapOf() else null
    @PublishedApi internal inline fun getTask(name:String):Task? = (_tasks ?: _voTasks[type])?.get(name)
    /** ::class 캐쉬용 */
    @PublishedApi internal var _type:KClass<out VO>? = null
    inline val type:KClass<out VO> get() = _type ?: this::class.also { _type = it }
    /** lazy 필드 매칭용 인덱서 */
    @PublishedApi internal var __index__ = -1 /** 델리게이터 정의 순번. -1은 아직 한번도 정의되지 않은 상태*/
    @PublishedApi internal var __field__:Field<*>? = null /**프로바이더에게 넘겨줄 필드정보 */
    @PublishedApi internal var __task__:Task? = null /** 프로바이더에게 넘겨줄 태스크 정보 */
    @PublishedApi internal var __keys__:ArrayList<String>? = null /**프로바이더에게 넘겨줄 델리게이터 선언 순번별 속성명 */
    /** 표준 델리게이터 생성기 */
    inline fun <TASK:Task, VALUE:Any> delegate(field:Field<VALUE>, block:TASK.()->Unit, task:()->TASK):Prop<VALUE>{
        /** voKeys에는 모든 VO의 속성리스트가 들어있고 delegate호출시마다 점진적으로 리스트가 증가함
         * 만약 이 VO클래스가 처음으로 만들어지는 것이라면 인덱스 -1 상황에서 voKeys[type]도 널임
         * 한 번이라도 델리게이터프로바이더가 호출되었다면 그 때마다 속성을 __keys__에 업데이트하면서 이를 voKey에도 계속 넣어줌
         * __시리즈는 전부 인스턴스 생성시에만 사용되는 인스턴스 저장소라 동시성 문제가 없음
         * 단 voXXX시리즈는 최초 VO타입별 생성시에 동시성 이슈가 생길 수 있으나
         * map계열은 같은 값을 덮을 뿐이고 유일한 
         */
        if(__index__ == -1 || _voKeys[type]?.size == __index__ + 1) task().also{__task__ = it}.block()
        return delegate(field)
    }
    inline fun <TASK:Task, VALUE:Any> delegate(field:Field<VALUE>, block:()->TASK):Prop<VALUE>{
        if(__index__ == -1 || _voKeys[type]?.size == __index__ + 1) __task__ = block()
        return delegate(field)
    }
    inline fun <VALUE:Any> delegate(field: Field<VALUE>):Prop<VALUE>{
        if(__index__ == -1){
            if(type !in _voKeys) __keys__ = arrayListOf()
            __index__ = 0
        } else __index__++
        __field__ = field
        return _delegateProvider as Prop<VALUE>
    }
//    val enums = hashMapOf<KClass<*>, Array<*>>()
//    inline fun <reified ENUM:Enum<ENUM>> enum(block: EnumField<ENUM>.()->Unit = {}): Prop<ENUM> {
//        if(ENUM::class !in enums) enums[ENUM::class] = enumValues<ENUM>()
//        EnumField<ENUM>().firstTask()?.block()
//        return EnumField<ENUM>().delegator
//    }
//    inline fun <reified ENUM:Enum<ENUM>> enumList(block: EnumListField<ENUM>.()->Unit = {}): Prop<MutableList<ENUM>> {
//        if(ENUM::class !in enums) enums[ENUM::class] = enumValues<ENUM>()
//        EnumListField<ENUM>().firstTask()?.block()
//        return EnumListField<ENUM>().delegator
//    }
//    inline fun <reified ENUM:Enum<ENUM>> enumMap(block: EnumMapField<ENUM>.()->Unit = {}): Prop<MutableMap<String, ENUM>> {
//        if(ENUM::class !in enums) enums[ENUM::class] = enumValues<ENUM>()
//        EnumMapField<ENUM>().firstTask()?.block()
//        return EnumMapField<ENUM>().delegator
//    }
//    inline fun <reified DATA: VO> data(noinline factory:()->DATA, block: DataField<DATA>.()->Unit = {}): Prop<DATA> {
//        DataField[factory].firstTask()?.block()
//        return DataField[factory].delegator
//    }
//    inline fun <DATA: VO> data(cls:KClass<DATA>, noinline factory:()->DATA, block: DataField<DATA>.()->Unit = {}): Prop<DATA> {
//        DataField[cls, factory].firstTask()?.block()
//        return DataField[cls, factory].delegator
//    }
//    inline fun <reified DATA: VO> dataList(noinline factory:()->DATA, block: DataListField<DATA>.()->Unit = {}): Prop<MutableList<DATA>> {
//        DataListField[factory].firstTask()?.block()
//        return DataListField[factory].delegator
//    }
//    inline fun <reified DATA: VO> dataMap(noinline factory:()->DATA, block: DataMapField<DATA>.()->Unit = {}): Prop<MutableMap<String, DATA>> {
//        DataMapField[factory].firstTask()?.block()
//        return DataMapField[factory].delegator
//    }
//    inline fun <DATA: VO> dataMap(cls:KClass<DATA>, noinline factory:()->DATA, block: DataMapField<DATA>.()->Unit = {}): Prop<MutableMap<String, DATA>> {
//        DataMapField[cls, factory].firstTask()?.block()
//        return DataMapField[cls, factory].delegator
//    }
//    inline fun <reified DATA: VO> union(union: Union<DATA>, block: UnionField<DATA>.()->Unit = {}): Prop<DATA> {
//        UnionField[union].firstTask()?.block()
//        return UnionField[union].delegator
//    }
//    inline fun <reified DATA: VO> unionList(union: Union<DATA>, block: UnionListField<DATA>.()->Unit = {}): Prop<MutableList<DATA>> {
//        UnionListField[union].firstTask()?.block()
//        return UnionListField[union].delegator
//    }
//    inline fun <reified DATA: VO> unionMap(union: Union<DATA>, block: UnionMapField<DATA>.()->Unit = {}): Prop<MutableMap<String, DATA>> {
//        UnionMapField[union].firstTask()?.block()
//        return UnionMapField[union].delegator
//    }

//    inline fun intMap(vararg items:Pair<String, Int>, block: IntMapField.()->Unit = {}): Prop<HashMap<String, Int>> {
//        IntMapField.firstTask()?.apply{
//            block()
//            default{HashMap<String, Int>(items.size).also{it.putAll(items)}}
//        }
//        return IntMapField.delegator
//    }
//    inline fun uintMap(vararg items:Pair<String, UInt>, block: UIntMapField.()->Unit = {}): Prop<HashMap<String, UInt>> {
//        UIntMapField.firstTask()?.apply{
//            block()
//            default{HashMap<String, UInt>(items.size).also{it.putAll(items)}}
//        }
//        return UIntMapField.delegator
//    }
//    inline fun longMap(vararg items:Pair<String, Long>, block: LongMapField.()->Unit = {}): Prop<HashMap<String, Long>> {
//        LongMapField.firstTask()?.apply{
//            block()
//            default{HashMap<String, Long>(items.size).also{it.putAll(items)}}
//        }
//        return LongMapField.delegator
//    }
//    inline fun ulongMap(vararg items:Pair<String, ULong>, block: ULongMapField.()->Unit = {}): Prop<HashMap<String, ULong>> {
//        ULongMapField.firstTask()?.apply{
//            block()
//            default{HashMap<String, ULong>(items.size).also{it.putAll(items)}}
//        }
//        return ULongMapField.delegator
//    }
//    inline fun shortMap(vararg items:Pair<String, Short>, block: ShortMapField.()->Unit = {}): Prop<HashMap<String, Short>> {
//        ShortMapField.firstTask()?.apply{
//            block()
//            default{HashMap<String, Short>(items.size).also{it.putAll(items)}}
//        }
//        return ShortMapField.delegator
//    }
//    inline fun ushortMap(vararg items:Pair<String, UShort>, block: UShortMapField.()->Unit = {}): Prop<HashMap<String, UShort>> {
//        UShortMapField.firstTask()?.apply{
//            block()
//            default{HashMap<String, UShort>(items.size).also{it.putAll(items)}}
//        }
//        return UShortMapField.delegator
//    }
//    inline fun floatMap(vararg items:Pair<String, Float>, block: FloatMapField.()->Unit = {}): Prop<HashMap<String, Float>> {
//        FloatMapField.firstTask()?.apply{
//            block()
//            default{HashMap<String, Float>(items.size).also{it.putAll(items)}}
//        }
//        return FloatMapField.delegator
//    }
//    inline fun doubleMap(vararg items:Pair<String, Double>, block: DoubleMapField.()->Unit = {}): Prop<HashMap<String, Double>> {
//        DoubleMapField.firstTask()?.apply{
//            block()
//            default{HashMap<String, Double>(items.size).also{it.putAll(items)}}
//        }
//        return DoubleMapField.delegator
//    }
//    inline fun booleanMap(vararg items:Pair<String, Boolean>, block: BooleanMapField.()->Unit = {}): Prop<HashMap<String, Boolean>> {
//        BooleanMapField.firstTask()?.apply{
//            block()
//            default{HashMap<String, Boolean>(items.size).also{it.putAll(items)}}
//        }
//        return BooleanMapField.delegator
//    }
//    inline fun stringMap(vararg items:Pair<String, String>, block: StringMapField.()->Unit = {}): Prop<HashMap<String, String>> {
//        StringMapField.firstTask()?.apply{
//            block()
//            default{HashMap<String, String>(items.size).also{it.putAll(items)}}
//        }
//        return StringMapField.delegator
//    }
//    inline fun <reified ENUM:Enum<ENUM>> enum(v:ENUM, block: EnumField<ENUM>.()->Unit = {}): Prop<ENUM> {
//        EnumField<ENUM>().firstTask()?.apply{
//            block()
//            default(v)
//        }
//        return EnumField<ENUM>().delegator
//    }
//    inline fun <reified ENUM:Enum<ENUM>> enumList(vararg items:ENUM, block: EnumListField<ENUM>.()->Unit = {}): Prop<MutableList<ENUM>> {
//        EnumListField<ENUM>().firstTask()?.apply{
//            block()
//            default{ArrayList<ENUM>(items.size).also{it.addAll(items)}}
//        }
//        return EnumListField<ENUM>().delegator
//    }
//    inline fun <reified ENUM:Enum<ENUM>> enumMap(vararg items:Pair<String, ENUM>, block: EnumMapField<ENUM>.()->Unit = {}): Prop<MutableMap<String, ENUM>> {
//        EnumMapField<ENUM>().firstTask()?.apply{
//            block()
//            default{HashMap<String, ENUM>(items.size).also{it.putAll(items)}}
//        }
//        return EnumMapField<ENUM>().delegator
//    }
}
