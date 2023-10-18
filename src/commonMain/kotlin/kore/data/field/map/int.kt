@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.map

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task

object IntListField: Field<MutableList<Int>> {
    class T: Task(){
        fun default(v:MutableList<Int>){
            _default = Task.Default{_,_->ArrayList<Int>(v.size).also{it.addAll(v)}}
        }
    }
}
inline val VO.intList:Prop<MutableList<Int>> get() = delegate(IntListField)
inline fun VO.intList(v:MutableList<Int>):Prop<MutableList<Int>>
        = delegate(IntListField){IntListField.T().also{it.default(v)}}
inline fun VO.intList(block: IntListField.T.()->Unit):Prop<MutableList<Int>> = delegate(IntListField, block){IntListField.T()}