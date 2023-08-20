package ein2b.core.entity.field

import ein2b.core.entity.task.ConvertTask
import ein2b.core.entity.task.Tasks

class FieldSet(private val tasks: Tasks){
    fun add(task: ConvertTask){
        (tasks.setTasks ?: arrayListOf<ConvertTask>().also{ tasks.setTasks = it}).add(task)
    }
}