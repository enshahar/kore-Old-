@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.value

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task

object ShortField: Field<Short> {
    class T: Task(){
        fun default(v:Short){
            _default = v
        }
    }
}
inline val VO.short:Prop<Short> get() = delegate(ShortField)
inline fun VO.short(v:Short):Prop<Short> = delegate(ShortField){ ShortField.T().also{it.default(v)}}
inline fun VO.short(block: ShortField.T.()->Unit):Prop<Short> = delegate(ShortField, block){ ShortField.T()}