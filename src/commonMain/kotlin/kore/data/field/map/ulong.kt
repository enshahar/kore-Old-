@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.map

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task

object ULongListField: Field<MutableList<ULong>> {
    class T: Task(){
        fun default(v:MutableList<ULong>){
            _default = Task.Default{_,_->ArrayList<ULong>(v.size).also{it.addAll(v)}}
        }
    }
}
inline val VO.ulongList:Prop<MutableList<ULong>> get() = delegate(ULongListField)
inline fun VO.ulongList(v:MutableList<ULong>):Prop<MutableList<ULong>>
        = delegate(ULongListField){ULongListField.T().also{it.default(v)}}
inline fun VO.ulongList(block: ULongListField.T.()->Unit):Prop<MutableList<ULong>> = delegate(ULongListField, block){ULongListField.T()}