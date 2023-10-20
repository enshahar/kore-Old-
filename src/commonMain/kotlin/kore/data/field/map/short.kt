@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.map

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task
import kore.data.task.Task.Default

object ShortMapField: Field<MutableMap<String, Short>> {
    class T: Task(){
        fun default(v:MutableMap<String, Short>){
            _default = Default{_,_->HashMap<String, Short>(v.size).also{it.putAll(v)}}
        }
        fun default(vararg v:Pair<String, Short>){
            _default = Default{_,_->HashMap<String, Short>(v.size).also{it.putAll(v)}}
        }
    }
}
inline val VO.shortMap:Prop<MutableMap<String, Short>> get() = delegate(ShortMapField)
inline fun VO.shortMap(v:MutableMap<String, Short>):Prop<MutableMap<String, Short>>
        = delegate(ShortMapField){ShortMapField.T().also{it.default(v)}}
inline fun VO.shortMap(vararg v:Pair<String, Short>):Prop<MutableMap<String, Short>>
        = delegate(ShortMapField){ShortMapField.T().also{it.default(*v)}}
inline fun VO.shortMap(block: ShortMapField.T.()->Unit):Prop<MutableMap<String, Short>> = delegate(ShortMapField, block){ShortMapField.T()}