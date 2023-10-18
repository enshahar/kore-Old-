@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.value

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task

object UShortField: Field<UShort> {
    class T: Task(){
        fun default(v:UShort){
            _default = v
        }
    }
}
inline val VO.ushort:Prop<UShort> get() = delegate(UShortField)
inline fun VO.ushort(v:UShort):Prop<UShort> = delegate(UShortField){ UShortField.T().also{it.default(v)}}
inline fun VO.ushort(block: UShortField.T.()->Unit):Prop<UShort> = delegate(UShortField, block){ UShortField.T()}