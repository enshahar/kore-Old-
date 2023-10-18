@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.map

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task

object LongListField: Field<MutableList<Long>> {
    class T: Task(){
        fun default(v:MutableList<Long>){
            _default = Task.Default{_,_->ArrayList<Long>(v.size).also{it.addAll(v)}}
        }
    }
}
inline val VO.longList:Prop<MutableList<Long>> get() = delegate(LongListField)
inline fun VO.longList(v:MutableList<Long>):Prop<MutableList<Long>>
        = delegate(LongListField){LongListField.T().also{it.default(v)}}
inline fun VO.longList(block: LongListField.T.()->Unit):Prop<MutableList<Long>> = delegate(LongListField, block){LongListField.T()}