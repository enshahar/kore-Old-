@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.map

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task
import kore.data.task.Task.Default

object LongMapField: Field<MutableMap<String, Long>> {
    class T: Task(){
        fun default(v:MutableMap<String, Long>){
            _default = Default{_,_->HashMap<String, Long>(v.size).also{it.putAll(v)}}
        }
        fun default(vararg v:Pair<String, Long>){
            _default = Default{_,_->HashMap<String, Long>(v.size).also{it.putAll(v)}}
        }
    }
}
inline val VO.longMap:Prop<MutableMap<String, Long>> get() = delegate(LongMapField)
inline fun VO.longMap(v:MutableMap<String, Long>):Prop<MutableMap<String, Long>>
        = delegate(LongMapField){LongMapField.T().also{it.default(v)}}
inline fun VO.longMap(vararg v:Pair<String, Long>):Prop<MutableMap<String, Long>>
        = delegate(LongMapField){LongMapField.T().also{it.default(*v)}}
inline fun VO.longMap(block: LongMapField.T.()->Unit):Prop<MutableMap<String, Long>> = delegate(LongMapField, block){LongMapField.T()}