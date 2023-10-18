@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.value

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task

object LongField: Field<Long> {
    class T: Task(){
        fun default(v:Long){
            _default = v
        }
    }
}
inline val VO.long:Prop<Long> get() = delegate(LongField)
inline fun VO.long(v:Long):Prop<Long> = delegate(LongField){ LongField.T().also{it.default(v)}}
inline fun VO.long(block: LongField.T.()->Unit):Prop<Long> = delegate(LongField, block){ LongField.T()}