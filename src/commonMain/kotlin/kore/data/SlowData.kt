package kore.data

import kore.data.field.Field
import kore.data.task.Task

abstract class SlowData: Data(){
    val _tasks:HashMap<Int, Task> = hashMapOf()
}