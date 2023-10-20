@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.list

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.field.list.DoubleListField.T
import kore.data.task.Task

object DoubleListField: Field<MutableList<Double>> {
    class T: Task(){
        fun default(v:MutableList<Double>){
            _default = Task.Default{_,_->ArrayList<Double>(v.size).also{it.addAll(v)}}
        }
        fun default(vararg v:Double){
            _default = Task.Default{_,_->ArrayList<Double>(v.size).also{a->v.forEach{a.add(it)}}}
        }
    }
}
inline val VO.doubleList:Prop<MutableList<Double>> get() = delegate(DoubleListField)
inline fun VO.doubleList(v:MutableList<Double>):Prop<MutableList<Double>>
= delegate(DoubleListField){ T().also{it.default(v)}}
inline fun VO.doubleList(vararg v:Double):Prop<MutableList<Double>>
= delegate(DoubleListField){ T().also{it.default(*v)}}
inline fun VO.doubleList(block: T.()->Unit):Prop<MutableList<Double>> = delegate(DoubleListField, block){ T() }