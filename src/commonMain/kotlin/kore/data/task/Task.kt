@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")

package kore.data.task

import kore.data.Data

class Task{
    var default:Any? = null
    var setTasks:ArrayList<(Data, Any)->Any?>? = null
    var getTasks:ArrayList<(Data, Any)->Any?>? = null
    var include:((Data)->Boolean)? = null
    inline fun getDefault(data:Data) = (default as? Function1<Data, Any>)?.invoke(data) ?: default
}