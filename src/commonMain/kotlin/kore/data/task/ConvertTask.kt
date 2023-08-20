package ein2b.core.entity.task

interface ConvertTask{
    operator fun invoke(value:Any):Any?
}