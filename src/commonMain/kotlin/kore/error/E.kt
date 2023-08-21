package kore.error

abstract class E(vararg items:Any):Throwable(){
    val data:ArrayList<Any> = arrayListOf(this::class.simpleName!!)
    init{
        data.addAll(items)
    }
    override val message: String? get() = data.joinToString(" ")

}