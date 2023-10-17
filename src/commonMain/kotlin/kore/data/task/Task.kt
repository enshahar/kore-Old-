@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")

package kore.data.task

import kore.data.VO

class Task{
    var default:Any? = null
    var setTasks:ArrayList<(VO, Any)->Any?>? = null
    var getTasks:ArrayList<(VO, Any)->Any?>? = null
    var include:((VO)->Boolean)? = null
    inline fun getDefault(data:VO) = (default as? Function1<VO, Any>)?.invoke(data) ?: default
}