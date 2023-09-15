import kotlin.collections.List as KList
import kotlin.math.pow

sealed class Option<out VALUE:Any> {
    companion object{
        inline operator fun <VALUE:Any> invoke():Option<VALUE> = None
        inline operator fun <VALUE:Any> invoke(value:VALUE):Option<VALUE> = Some(value)
        fun <VALUE:Any, OTHER:Any> lift(block:(VALUE)->OTHER):(Option<VALUE>)->Option<OTHER> = {it.map(block)}
        fun <VALUE:Any> catches(block:()->VALUE):Option<VALUE> = try{
            Option(block())
        }catch (e:Throwable) {
            None
        }
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
inline fun <VALUE:Any, OTHER:Any, RETURN:Any> Option<VALUE>.map2(other:Option<OTHER>, block:(VALUE, OTHER)->RETURN):Option<RETURN> = when(this){
    is Option.None->Option()
    is Option.Some->when(other){
        is Option.None->Option()
        is Option.Some->Option(block(value, other.value))
    }
}
inline fun <VALUE:Any, OTHER:Any, RETURN:Any> Option<VALUE>.map2F(other:Option<OTHER>, block:(VALUE, OTHER)->RETURN):Option<RETURN>
= fold({Option()}){when(other){
        is Option.None->Option()
        is Option.Some->Option(block(it.value, other.value))
    }
}

inline fun <VALUE:Any> List<Option<VALUE>>.sequence():Option<List<VALUE>> = Option(flatMap{
    when(it){
        is Option.None->List.Nil
        is Option.Some->List.Cons(it.value, List.Nil)
    }
})
inline fun KList<Double>.variance():Option<Double> = if(isEmpty()) Option() else{
    val avg = average()
    Option(map{(it - avg).pow(2)}.average())
}
fun List<Double>.average():Option<Double> = when(val s = size){
    0 -> Option()
    else -> Option(sum() / s)
}
fun List<Double>.variance():Option<Double> = when(val avg = average()){
    is Option.None->avg
    is Option.Some->map{(it - avg.value).pow(2)}.average()
}