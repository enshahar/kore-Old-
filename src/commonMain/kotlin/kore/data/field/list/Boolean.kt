@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.list

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.field.list.BooleanListField.T
import kore.data.task.Task

object BooleanListField: Field<MutableList<Boolean>> {
    class T: Task(){
        fun default(v:MutableList<Boolean>){
            _default = Task.Default{_,_->ArrayList<Boolean>(v.size).also{it.addAll(v)}}
        }
        fun default(vararg v:Boolean){
            _default = Task.Default{_,_->ArrayList<Boolean>(v.size).also{a->v.forEach{a.add(it)}}}
        }
    }
}
inline val VO.booleanList:Prop<MutableList<Boolean>> get() = delegate(BooleanListField)
inline fun VO.booleanList(v:MutableList<Boolean>):Prop<MutableList<Boolean>>
= delegate(BooleanListField){ T().also{it.default(v)}}
inline fun VO.booleanList(vararg v:Boolean):Prop<MutableList<Boolean>>
= delegate(BooleanListField){ T().also{it.default(*v)}}
inline fun VO.booleanList(block: T.()->Unit):Prop<MutableList<Boolean>> = delegate(BooleanListField, block){ T() }