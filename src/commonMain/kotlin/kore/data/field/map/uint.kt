@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.map

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task
import kore.data.task.Task.Default

object UIntMapField: Field<MutableMap<String, UInt>> {
    class T: Task(){
        fun default(v:MutableMap<String, UInt>){
            _default = Default{_,_->HashMap<String, UInt>(v.size).also{it.putAll(v)}}
        }
        fun default(vararg v:Pair<String, UInt>){
            _default = Default{_,_->HashMap<String, UInt>(v.size).also{it.putAll(v)}}
        }
    }
}
inline val VO.uintMap:Prop<MutableMap<String, UInt>> get() = delegate(UIntMapField)
inline fun VO.uintMap(v:MutableMap<String, UInt>):Prop<MutableMap<String, UInt>>
        = delegate(UIntMapField){UIntMapField.T().also{it.default(v)}}
inline fun VO.uintMap(vararg v:Pair<String, UInt>):Prop<MutableMap<String, UInt>>
        = delegate(UIntMapField){UIntMapField.T().also{it.default(*v)}}
inline fun VO.uintMap(block: UIntMapField.T.()->Unit):Prop<MutableMap<String, UInt>> = delegate(UIntMapField, block){UIntMapField.T()}