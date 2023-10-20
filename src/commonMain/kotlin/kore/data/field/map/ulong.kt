@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.map

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task
import kore.data.task.Task.Default

object ULongMapField: Field<MutableMap<String, ULong>> {
    class T: Task(){
        fun default(v:MutableMap<String, ULong>){
            _default = Default{_,_->HashMap<String, ULong>(v.size).also{it.putAll(v)}}
        }
        fun default(vararg v:Pair<String, ULong>){
            _default = Default{_,_->HashMap<String, ULong>(v.size).also{it.putAll(v)}}
        }
    }
}
inline val VO.ulongMap:Prop<MutableMap<String, ULong>> get() = delegate(ULongMapField)
inline fun VO.ulongMap(v:MutableMap<String, ULong>):Prop<MutableMap<String, ULong>>
        = delegate(ULongMapField){ULongMapField.T().also{it.default(v)}}
inline fun VO.ulongMap(vararg v:Pair<String, ULong>):Prop<MutableMap<String, ULong>>
        = delegate(ULongMapField){ULongMapField.T().also{it.default(*v)}}
inline fun VO.ulongMap(block: ULongMapField.T.()->Unit):Prop<MutableMap<String, ULong>> = delegate(ULongMapField, block){ULongMapField.T()}