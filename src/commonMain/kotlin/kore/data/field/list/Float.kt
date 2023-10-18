@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.list

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.field.list.FloatListField.T
import kore.data.task.Task

object FloatListField: Field<MutableList<Float>> {
    class T: Task(){
        fun default(v:MutableList<Float>){
            _default = Task.Default{_,_->ArrayList<Float>(v.size).also{it.addAll(v)}}
        }
        fun default(vararg v:Float){
            _default = Task.Default{_,_->ArrayList<Float>(v.size).also{a->v.forEach{a.add(it)}}}
        }
    }
}
inline val VO.floatList:Prop<MutableList<Float>> get() = delegate(FloatListField)
inline fun VO.floatList(v:MutableList<Float>):Prop<MutableList<Float>>
= delegate(FloatListField){ T().also{it.default(v)}}
inline fun VO.floatList(vararg v:Float):Prop<MutableList<Float>>
= delegate(FloatListField){ T().also{it.default(*v)}}
inline fun VO.floatList(block: T.()->Unit):Prop<MutableList<Float>> = delegate(FloatListField, block){ T() }