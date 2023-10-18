@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.value

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task

object UIntField: Field<UInt> {
    class T: Task(){
        fun default(v:UInt){
            _default = v
        }
    }
}
inline val VO.uint:Prop<UInt> get() = delegate(UIntField)
inline fun VO.uint(v:UInt):Prop<UInt> = delegate(UIntField){ UIntField.T().also{it.default(v)}}
inline fun VO.uint(block: UIntField.T.()->Unit):Prop<UInt> = delegate(UIntField, block){ UIntField.T()}