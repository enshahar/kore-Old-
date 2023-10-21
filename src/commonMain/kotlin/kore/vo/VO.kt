@file:Suppress("NOTHING_TO_INLINE", "PropertyName", "UNCHECKED_CAST", "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
    "ObjectPropertyName"
)

package kore.vo

import kore.vo.field.*
import kore.vo.task.Task
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
            override fun getValue(vo: VO, property: KProperty<*>): Any = vo[property.name] ?: Task.NoDefault(vo, property.name).terminate()
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
    override fun toString(): String =
"""${super.toString()}-${values.toList().joinToString {(k,v)->"$k:$v"}}    
"""

    /** 속성 getter, setter*/
    operator fun set(key:String, value:Any){
        values[key] = getTask(key)?.setFold(this, key, value) ?: value
    }
    operator fun get(key:String):Any? = getTask(key)?.let{
        (values[key] ?: it.getDefault(this, key))?.let{v->it.getFold(this, key, v)}
    } ?: values[key]

    /** 외부에 표출되는 저장소 */
    inline val props:Map<String, Any?> get() = values
    /** 인스턴스 필드 저장소를 쓸 경우 */
    @PublishedApi internal val _fields:HashMap<String, Field<*>>? = if(useInstanceField) hashMapOf() else null
    @PublishedApi internal val _tasks:HashMap<String, Task>? = if(useInstanceField) hashMapOf() else null
    @PublishedApi internal inline fun getTask(name:String):Task? = getTasks()?.get(name)
    @PublishedApi internal inline fun getTasks():HashMap<String, Task>? = _tasks ?: _voTasks[type]
    @PublishedApi internal inline fun getFields():HashMap<String, Field<*>>? = _fields ?: _voFields[type]
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
         * map계열은 같은 값을 덮을 뿐이고 속성명 리스트는 경쟁하는 각 인스턴스가 자기 내부의 리스트를 게속 업데이트함
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
}
