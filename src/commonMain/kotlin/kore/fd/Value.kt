sealed class Value<out T>:()->T{
    companion object{
        operator fun <T> invoke(value:T): Value<T> = Evaluated(value)
        operator fun <T> invoke(block:()->T): Value<T> = Unevaluated(block)
    }
    data class Evaluated<T> internal constructor(private val value:T): Value<T>(){
        override fun invoke(): T = value
    }
    data class Unevaluated<T> internal constructor(private val block:()->T): Value<T>(){
        private var memo:T? = null
        override fun invoke(): T  = memo ?: block().also { memo = it }
    }
}