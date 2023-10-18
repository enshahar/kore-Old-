@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE", "UNCHECKED_CAST", "UNCHECKED_CAST")

package kore.data.task

import kore.data.VO

abstract class Task{
    companion object{
        private val _include:(VO)->Boolean = {true}
        private val _exclude:(VO)->Boolean = {false}
    }
    @PublishedApi internal var _default:Any? = null
    var setTasks:ArrayList<(VO, Any)->Any?>? = null
        internal set
    var getTasks:ArrayList<(VO, Any)->Any?>? = null
        internal set
    var include:(VO)->Boolean = _include
        internal set
    inline fun default(data:VO) = (_default as? Function1<VO, Any>)?.invoke(data) ?: _default
    fun exclude(){
        include = _exclude
    }
    fun isInclude(block:(VO)->Boolean){
        include = block
    }
}
class IntTask:Task(){
    fun default(v:Int){
        _default = v
    }
}