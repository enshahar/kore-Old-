@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.map

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task

object DoubleListField: Field<MutableList<Double>> {
    class T: Task(){
        fun default(v:MutableList<Double>){
            _default = Task.Default{_,_->ArrayList<Double>(v.size).also{it.addAll(v)}}
        }
    }
}
inline val VO.doubleList:Prop<MutableList<Double>> get() = delegate(DoubleListField)
inline fun VO.doubleList(v:MutableList<Double>):Prop<MutableList<Double>>
        = delegate(DoubleListField){DoubleListField.T().also{it.default(v)}}
inline fun VO.doubleList(block: DoubleListField.T.()->Unit):Prop<MutableList<Double>> = delegate(DoubleListField, block){DoubleListField.T()}