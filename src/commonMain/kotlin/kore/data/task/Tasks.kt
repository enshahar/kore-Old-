package kore.data.task


class Tasks{
    //var vali:eVali? = null
    var default:Any? = null
    var setTasks:ArrayList<(Any)->Any?>? = null
    var getTasks:ArrayList<(Any)->Any?>? = null
    var include:(()->Boolean)? = null
}