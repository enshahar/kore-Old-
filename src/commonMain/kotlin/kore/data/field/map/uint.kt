@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.map

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task

object UIntListField: Field<MutableList<UInt>> {
    class T: Task(){
        fun default(v:MutableList<UInt>){
            _default = Task.Default{_,_->ArrayList<UInt>(v.size).also{it.addAll(v)}}
        }
    }
}
inline val VO.uintList:Prop<MutableList<UInt>> get() = delegate(UIntListField)
inline fun VO.uintList(v:MutableList<UInt>):Prop<MutableList<UInt>>
        = delegate(UIntListField){UIntListField.T().also{it.default(v)}}
inline fun VO.uintList(block: UIntListField.T.()->Unit):Prop<MutableList<UInt>> = delegate(UIntListField, block){UIntListField.T()}