@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.map

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task
import kore.data.task.Task.Default

object FloatMapField: Field<MutableMap<String, Float>> {
    class T: Task(){
        fun default(v:MutableMap<String, Float>){
            _default = Default{_,_->HashMap<String, Float>(v.size).also{it.putAll(v)}}
        }
        fun default(vararg v:Pair<String, Float>){
            _default = Default{_,_->HashMap<String, Float>(v.size).also{it.putAll(v)}}
        }
    }
}
inline val VO.floatMap:Prop<MutableMap<String, Float>> get() = delegate(FloatMapField)
inline fun VO.floatMap(v:MutableMap<String, Float>):Prop<MutableMap<String, Float>>
        = delegate(FloatMapField){FloatMapField.T().also{it.default(v)}}
inline fun VO.floatMap(vararg v:Pair<String, Float>):Prop<MutableMap<String, Float>>
        = delegate(FloatMapField){FloatMapField.T().also{it.default(*v)}}
inline fun VO.floatMap(block: FloatMapField.T.()->Unit):Prop<MutableMap<String, Float>> = delegate(FloatMapField, block){FloatMapField.T()}