@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.map

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task

object UShortListField: Field<MutableList<UShort>> {
    class T: Task(){
        fun default(v:MutableList<UShort>){
            _default = Task.Default{_,_->ArrayList<UShort>(v.size).also{it.addAll(v)}}
        }
    }
}
inline val VO.ushortList:Prop<MutableList<UShort>> get() = delegate(UShortListField)
inline fun VO.ushortList(v:MutableList<UShort>):Prop<MutableList<UShort>>
        = delegate(UShortListField){UShortListField.T().also{it.default(v)}}
inline fun VO.ushortList(block: UShortListField.T.()->Unit):Prop<MutableList<UShort>> = delegate(UShortListField, block){UShortListField.T()}