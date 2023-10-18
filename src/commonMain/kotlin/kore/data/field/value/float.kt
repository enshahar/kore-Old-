@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.value

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task

object FloatField: Field<Float> {
    class T: Task(){
        fun default(v:Float){
            _default = v
        }
    }
}
inline val VO.float:Prop<Float> get() = delegate(FloatField)
inline fun VO.float(v:Float):Prop<Float> = delegate(FloatField){ FloatField.T().also{it.default(v)}}
inline fun VO.float(block: FloatField.T.()->Unit):Prop<Float> = delegate(FloatField, block){ FloatField.T()}