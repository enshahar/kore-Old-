@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.value

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task

object DoubleField: Field<Double> {
    class T: Task(){
        fun default(v:Double){
            _default = v
        }
    }
}
inline val VO.double:Prop<Double> get() = delegate(DoubleField)
inline fun VO.double(v:Double):Prop<Double> = delegate(DoubleField){ DoubleField.T().also{it.default(v)}}
inline fun VO.double(block: DoubleField.T.()->Unit):Prop<Double> = delegate(DoubleField, block){ DoubleField.T()}