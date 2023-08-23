package kore.data

import kore.data.field.Field
import kore.data.task.Task

abstract class SlowData: Data(){
    val _fields:HashMap<String, Field<*>> = hashMapOf()
    val _tasks:HashMap<Int, Task> = hashMapOf()
}