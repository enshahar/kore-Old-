package kore.data.task


class Task{
    //var vali:eVali? = null
    var default:Any? = null
    var setTasks:ArrayList<(Any)->Any?>? = null
    var getTasks:ArrayList<(Any)->Any?>? = null
    var include:(()->Boolean)? = null
}