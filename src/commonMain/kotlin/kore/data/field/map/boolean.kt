@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.map

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task
import kore.data.task.Task.Default

object BooleanMapField: Field<MutableMap<String, Boolean>> {
    class T: Task(){
        fun default(v:MutableMap<String, Boolean>){
            _default = Default{_,_->HashMap<String, Boolean>(v.size).also{it.putAll(v)}}
        }
        fun default(vararg v:Pair<String, Boolean>){
            _default = Default{_,_->HashMap<String, Boolean>(v.size).also{it.putAll(v)}}
        }
    }
}
inline val VO.booleanMap:Prop<MutableMap<String, Boolean>> get() = delegate(BooleanMapField)
inline fun VO.booleanMap(v:MutableMap<String, Boolean>):Prop<MutableMap<String, Boolean>>
= delegate(BooleanMapField){BooleanMapField.T().also{it.default(v)}}
inline fun VO.booleanMap(vararg v:Pair<String, Boolean>):Prop<MutableMap<String, Boolean>>
= delegate(BooleanMapField){BooleanMapField.T().also{it.default(*v)}}
inline fun VO.booleanMap(block: BooleanMapField.T.()->Unit):Prop<MutableMap<String, Boolean>> = delegate(BooleanMapField, block){BooleanMapField.T()}