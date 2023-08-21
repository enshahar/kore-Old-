package ein2b.core.entity.field

import kore.data.task.Tasks

class FieldGet(private val tasks: Tasks){
    fun add(task: ConvertTask){
        (tasks.getTasks ?: arrayListOf<ConvertTask>().also{ tasks.getTasks = it}).add(task)
    }
}