package ein2b.core.entity.field

import kore.data.task.Task

class FieldGet(private val task: Task){
    fun add(task: ConvertTask){
        (this.task.getTasks ?: arrayListOf<ConvertTask>().also{ this.task.getTasks = it}).add(task)
    }
}