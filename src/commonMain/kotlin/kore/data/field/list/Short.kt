@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.list

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.field.list.ShortListField.T
import kore.data.task.Task

object ShortListField: Field<MutableList<Short>> {
    class T: Task(){
        fun default(v:MutableList<Short>){
            _default = Task.Default{_,_->ArrayList<Short>(v.size).also{it.addAll(v)}}
        }
        fun default(vararg v:Short){
            _default = Task.Default{_,_->ArrayList<Short>(v.size).also{a->v.forEach{a.add(it)}}}
        }
    }
}
inline val VO.shortList:Prop<MutableList<Short>> get() = delegate(ShortListField)
inline fun VO.shortList(v:MutableList<Short>):Prop<MutableList<Short>>
= delegate(ShortListField){ T().also{it.default(v)}}
inline fun VO.shortList(vararg v:Short):Prop<MutableList<Short>>
= delegate(ShortListField){ T().also{it.default(*v)}}
inline fun VO.shortList(block: T.()->Unit):Prop<MutableList<Short>> = delegate(ShortListField, block){ T() }