@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.map

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task
import kore.data.task.Task.Default

object StringMapField: Field<MutableMap<String, String>> {
    class T: Task(){
        fun default(v:MutableMap<String, String>){
            _default = Default{_,_->HashMap<String, String>(v.size).also{it.putAll(v)}}
        }
        fun default(vararg v:Pair<String, String>){
            _default = Default{_,_->HashMap<String, String>(v.size).also{it.putAll(v)}}
        }
    }
}
inline val VO.stringMap:Prop<MutableMap<String, String>> get() = delegate(StringMapField)
inline fun VO.stringMap(v:MutableMap<String, String>):Prop<MutableMap<String, String>>
        = delegate(StringMapField){StringMapField.T().also{it.default(v)}}
inline fun VO.stringMap(vararg v:Pair<String, String>):Prop<MutableMap<String, String>>
        = delegate(StringMapField){StringMapField.T().also{it.default(*v)}}
inline fun VO.stringMap(block: StringMapField.T.()->Unit):Prop<MutableMap<String, String>> = delegate(StringMapField, block){StringMapField.T()}