package ein2b.core.entity.field

import ein2b.core.entity.task.ConvertTask
import ein2b.core.entity.task.Tasks

class FieldGet(private val tasks: Tasks){
    fun add(task: ConvertTask){
        (tasks.getTasks ?: arrayListOf<ConvertTask>().also{ tasks.getTasks = it}).add(task)
    }
}