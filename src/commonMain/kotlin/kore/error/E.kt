package kore.error

@Suppress("NOTHING_TO_INLINE")
abstract class E(vararg items:Any):Throwable(){
    val data:ArrayList<Any> = arrayListOf(this::class.simpleName!!)
    init{
        data.addAll(items)
    }
    override val message: String? get() = data.joinToString(" ")
    inline fun terminate(): Nothing = throw this
}