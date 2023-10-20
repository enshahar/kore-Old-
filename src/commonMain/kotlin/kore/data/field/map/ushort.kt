@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.map

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task
import kore.data.task.Task.Default

object UShortMapField: Field<MutableMap<String, UShort>> {
    class T: Task(){
        fun default(v:MutableMap<String, UShort>){
            _default = Default{_,_->HashMap<String, UShort>(v.size).also{it.putAll(v)}}
        }
        fun default(vararg v:Pair<String, UShort>){
            _default = Default{_,_->HashMap<String, UShort>(v.size).also{it.putAll(v)}}
        }
    }
}
inline val VO.ushortMap:Prop<MutableMap<String, UShort>> get() = delegate(UShortMapField)
inline fun VO.ushortMap(v:MutableMap<String, UShort>):Prop<MutableMap<String, UShort>>
        = delegate(UShortMapField){UShortMapField.T().also{it.default(v)}}
inline fun VO.ushortMap(vararg v:Pair<String, UShort>):Prop<MutableMap<String, UShort>>
        = delegate(UShortMapField){UShortMapField.T().also{it.default(*v)}}
inline fun VO.ushortMap(block: UShortMapField.T.()->Unit):Prop<MutableMap<String, UShort>> = delegate(UShortMapField, block){UShortMapField.T()}