@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.map

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task
import kore.data.task.Task.Default

object IntMapField: Field<MutableMap<String, Int>> {
    class T: Task(){
        fun default(v:MutableMap<String, Int>){
            _default = Default{_,_->HashMap<String, Int>(v.size).also{it.putAll(v)}}
        }
        fun default(vararg v:Pair<String, Int>){
            _default = Default{_,_->HashMap<String, Int>(v.size).also{it.putAll(v)}}
        }
    }
}
inline val VO.intMap:Prop<MutableMap<String, Int>> get() = delegate(IntMapField)
inline fun VO.intMap(v:MutableMap<String, Int>):Prop<MutableMap<String, Int>>
        = delegate(IntMapField){IntMapField.T().also{it.default(v)}}
inline fun VO.intMap(vararg v:Pair<String, Int>):Prop<MutableMap<String, Int>>
        = delegate(IntMapField){IntMapField.T().also{it.default(*v)}}
inline fun VO.intMap(block: IntMapField.T.()->Unit):Prop<MutableMap<String, Int>> = delegate(IntMapField, block){IntMapField.T()}