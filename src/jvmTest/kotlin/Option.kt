import kotlin.collections.List
import kotlin.math.pow

sealed class Option<out VALUE:Any> {
    companion object{
        inline operator fun <VALUE:Any> invoke():Option<VALUE> = None
        inline operator fun <VALUE:Any> invoke(value:VALUE):Option<VALUE> = Some(value)

    }
    data object None:Option<Nothing>()
    data class  Some<out VALUE:Any>(val value: VALUE):Option<VALUE>()
}
inline fun <VALUE:Any, OTHER:Any> Option<VALUE>.map(block:(VALUE)->OTHER):Option<OTHER> = when(this){
    is Option.None->this
    is Option.Some->Option(block(value))
}
inline fun <VALUE:Any, OTHER:Any> Option<VALUE>.flatMap(block:(VALUE)->Option<OTHER>):Option<OTHER> = when(this){
    is Option.None->this
    is Option.Some->block(value)
}
inline fun <VALUE:Any> Option<VALUE>.getOrElse(block:()->VALUE):VALUE = when(this){
    is Option.None->block()
    is Option.Some->value
}
inline fun <VALUE:Any> Option<VALUE>.orElse(block:()->Option<VALUE>):Option<VALUE> = when(this){
    is Option.None->block()
    is Option.Some->this
}
inline fun <VALUE:Any> Option<VALUE>.filter(block:(VALUE)->Boolean):Option<VALUE> = when(this){
    is Option.None->this
    is Option.Some->if(block(value)) this else Option()
}
inline fun <VALUE:Any, OTHER:Any> Option<VALUE>.fold(noneBlock:()->OTHER, someBlock:(Option.Some<VALUE>)->OTHER):OTHER = when(this){
    is Option.None->noneBlock()
    is Option.Some->someBlock(this)
}
inline fun <VALUE:Any, OTHER:Any> Option<VALUE>.mapF(block:(VALUE)->OTHER):Option<OTHER> = fold({Option()}){Option(block(it.value))}
inline fun <VALUE:Any, OTHER:Any> Option<VALUE>.flatMapF(block:(VALUE)->Option<OTHER>):Option<OTHER> = fold({Option()}){block(it.value)}
inline fun <VALUE:Any> Option<VALUE>.getOrElseF(block:()->VALUE):VALUE = fold(block){it.value}
inline fun <VALUE:Any> Option<VALUE>.orElseF(block:()->Option<VALUE>):Option<VALUE> = fold(block){it}
inline fun <VALUE:Any> Option<VALUE>.filterF(block:(VALUE)->Boolean):Option<VALUE> = fold({Option()}){if(block(it.value)) this else Option()}
fun List<Double>.variance():Option<Double> = if(isEmpty()) Option() else{
    val avg = average()
    Option(map{(it - avg).pow(2)}.average())
}