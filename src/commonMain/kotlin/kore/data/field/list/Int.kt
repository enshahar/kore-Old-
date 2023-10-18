@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.list

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.field.list.IntListField.T
import kore.data.task.Task

object IntListField: Field<MutableList<Int>> {
    class T: Task(){
        fun default(v:MutableList<Int>){
            _default = Task.Default{_,_->ArrayList<Int>(v.size).also{it.addAll(v)}}
        }
        fun default(vararg v:Int){
            _default = Task.Default{_,_->ArrayList<Int>(v.size).also{a->v.forEach{a.add(it)}}}
        }
    }
}
inline val VO.intList:Prop<MutableList<Int>> get() = delegate(IntListField)
inline fun VO.intList(v:MutableList<Int>):Prop<MutableList<Int>>
= delegate(IntListField){ T().also{it.default(v)}}
inline fun VO.intList(vararg v:Int):Prop<MutableList<Int>>
= delegate(IntListField){ T().also{it.default(*v)}}
inline fun VO.intList(block: T.()->Unit):Prop<MutableList<Int>> = delegate(IntListField, block){ T() }