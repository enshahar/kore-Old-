@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.map

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.task.Task
import kore.data.task.Task.Default

object DoubleMapField: Field<MutableMap<String, Double>> {
    class T: Task(){
        fun default(v:MutableMap<String, Double>){
            _default = Default{_,_->HashMap<String, Double>(v.size).also{it.putAll(v)}}
        }
        fun default(vararg v:Pair<String, Double>){
            _default = Default{_,_->HashMap<String, Double>(v.size).also{it.putAll(v)}}
        }
    }
}
inline val VO.doubleMap:Prop<MutableMap<String, Double>> get() = delegate(DoubleMapField)
inline fun VO.doubleMap(v:MutableMap<String, Double>):Prop<MutableMap<String, Double>>
        = delegate(DoubleMapField){DoubleMapField.T().also{it.default(v)}}
inline fun VO.doubleMap(vararg v:Pair<String, Double>):Prop<MutableMap<String, Double>>
        = delegate(DoubleMapField){DoubleMapField.T().also{it.default(*v)}}
inline fun VO.doubleMap(block: DoubleMapField.T.()->Unit):Prop<MutableMap<String, Double>> = delegate(DoubleMapField, block){DoubleMapField.T()}