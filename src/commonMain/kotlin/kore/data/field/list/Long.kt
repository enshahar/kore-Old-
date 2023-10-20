@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.list

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.field.list.LongListField.T
import kore.data.task.Task

object LongListField: Field<MutableList<Long>> {
    class T: Task(){
        fun default(v:MutableList<Long>){
            _default = Task.Default{_,_->ArrayList<Long>(v.size).also{it.addAll(v)}}
        }
        fun default(vararg v:Long){
            _default = Task.Default{_,_->ArrayList<Long>(v.size).also{a->v.forEach{a.add(it)}}}
        }
    }
}
inline val VO.longList:Prop<MutableList<Long>> get() = delegate(LongListField)
inline fun VO.longList(v:MutableList<Long>):Prop<MutableList<Long>>
= delegate(LongListField){ T().also{it.default(v)}}
inline fun VO.longList(vararg v:Long):Prop<MutableList<Long>>
= delegate(LongListField){ T().also{it.default(*v)}}
inline fun VO.longList(block: T.()->Unit):Prop<MutableList<Long>> = delegate(LongListField, block){ T() }