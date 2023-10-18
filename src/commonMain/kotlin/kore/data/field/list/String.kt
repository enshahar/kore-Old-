@file:Suppress("NOTHING_TO_INLINE")

package kore.data.field.list

import kore.data.VO
import kore.data.field.Field
import kore.data.field.Prop
import kore.data.field.list.StringListField.T
import kore.data.task.Task

object StringListField: Field<MutableList<String>> {
    class T: Task(){
        fun default(v:MutableList<String>){
            _default = Task.Default{_,_->ArrayList<String>(v.size).also{it.addAll(v)}}
        }
        fun default(vararg v:String){
            _default = Task.Default{_,_->ArrayList<String>(v.size).also{a->v.forEach{a.add(it)}}}
        }
    }
}
inline val VO.stringList:Prop<MutableList<String>> get() = delegate(StringListField)
inline fun VO.stringList(v:MutableList<String>):Prop<MutableList<String>>
= delegate(StringListField){ T().also{it.default(v)}}
inline fun VO.stringList(vararg v:String):Prop<MutableList<String>>
= delegate(StringListField){ T().also{it.default(*v)}}
inline fun VO.stringList(block: T.()->Unit):Prop<MutableList<String>> = delegate(StringListField, block){ T() }