@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.list

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.field.list.ULongListField.T
import kore.data.task.Task

object ULongListField: Field<MutableList<ULong>> {
    class T: Task(){
        fun default(v:MutableList<ULong>){
            _default = Task.Default{_,_->ArrayList<ULong>(v.size).also{it.addAll(v)}}
        }
        @OptIn(ExperimentalUnsignedTypes::class)
        fun default(vararg v:ULong){
            _default = Task.Default{_,_->ArrayList<ULong>(v.size).also{a->v.forEach{a.add(it)}}}
        }
    }
}
inline val VO.ulongList:Prop<MutableList<ULong>> get() = delegate(ULongListField)
inline fun VO.ulongList(v:MutableList<ULong>):Prop<MutableList<ULong>>
= delegate(ULongListField){ T().also{it.default(v)}}
@OptIn(ExperimentalUnsignedTypes::class)
inline fun VO.ulongList(vararg v:ULong):Prop<MutableList<ULong>>
= delegate(ULongListField){ T().also{it.default(*v)}}
inline fun VO.ulongList(block: T.()->Unit):Prop<MutableList<ULong>> = delegate(ULongListField, block){ T() }