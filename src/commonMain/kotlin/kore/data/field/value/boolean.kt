@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.value

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task

object BooleanField: Field<Boolean> {
    class T: Task(){
        fun default(v:Boolean){
            _default = v
        }
    }
}
inline val VO.boolean:Prop<Boolean> get() = delegate(BooleanField)
inline fun VO.boolean(v:Boolean):Prop<Boolean> = delegate(BooleanField){ BooleanField.T().also{it.default(v)}}
inline fun VO.boolean(block: BooleanField.T.()->Unit):Prop<Boolean> = delegate(BooleanField, block){ BooleanField.T()}