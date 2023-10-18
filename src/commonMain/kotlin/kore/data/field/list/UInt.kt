@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.list

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.field.list.UIntListField.T
import kore.data.task.Task

object UIntListField: Field<MutableList<UInt>> {
    class T: Task(){
        fun default(v:MutableList<UInt>){
            _default = Task.Default{_,_->ArrayList<UInt>(v.size).also{it.addAll(v)}}
        }
        @OptIn(ExperimentalUnsignedTypes::class)
        fun default(vararg v:UInt){
            _default = Task.Default{_,_->ArrayList<UInt>(v.size).also{a->v.forEach{a.add(it)}}}
        }
    }
}
inline val VO.uintList:Prop<MutableList<UInt>> get() = delegate(UIntListField)
inline fun VO.uintList(v:MutableList<UInt>):Prop<MutableList<UInt>>
= delegate(UIntListField){ T().also{it.default(v)}}
@OptIn(ExperimentalUnsignedTypes::class)
inline fun VO.uintList(vararg v:UInt):Prop<MutableList<UInt>>
= delegate(UIntListField){ T().also{it.default(*v)}}
inline fun VO.uintList(block: T.()->Unit):Prop<MutableList<UInt>> = delegate(UIntListField, block){ T() }