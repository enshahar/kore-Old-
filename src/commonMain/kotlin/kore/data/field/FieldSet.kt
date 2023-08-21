package ein2b.core.entity.field

import kore.data.task.Task

class FieldSet(private val task: Task){
    fun add(task: ConvertTask){
        (this.task.setTasks ?: arrayListOf<ConvertTask>().also{ this.task.setTasks = it}).add(task)
    }
}