package ein2b.core.entity

class Error(
    val id:Any,
    override val message:String,
    vararg val result:Any
):Throwable()