import List.Cons
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

    inline fun getOrThrow():VALUE = when(this){
        is None->throw Throwable()
        is Some->value
    }
}
inline fun <VALUE:Any, OTHER:Any> Option<VALUE>.map(block:(VALUE)->OTHER):Option<OTHER> = when(this){
    is Option.None->this
    is Option.Some->Option(block(value))
}
inline fun <VALUE:Any> Option<VALUE>.getOrElse(block:()->VALUE):VALUE = when(this){
    is Option.None->block()
    is Option.Some->value
}
inline fun <VALUE:Any, OTHER:Any> Option<VALUE>.flatMap(block:(VALUE)->Option<OTHER>):Option<OTHER> = when(this){
    is Option.None->this
    is Option.Some->block(value)
}
inline fun <VALUE:Any> Option<VALUE>.orElse(block:()->Option<VALUE>):Option<VALUE> = when(this){
    is Option.None->block()
    is Option.Some->this
}
inline fun <VALUE:Any> Option<VALUE>.filter(block:(VALUE)->Boolean):Option<VALUE> = when(this){
    is Option.None->this
    is Option.Some->if(block(value)) this else Option()
}
inline fun <VALUE:Any, OTHER:Any> Option<VALUE>.flatMapF(block:(VALUE)->Option<OTHER>):Option<OTHER> = map(block).getOrElse { Option() }
inline fun <VALUE:Any> Option<VALUE>.orElseF(block:()->Option<VALUE>):Option<VALUE> = map{Option(it)}.getOrElse(block)
inline fun <VALUE:Any> Option<VALUE>.filterF(block:(VALUE)->Boolean):Option<VALUE> = flatMap{if(block(it)) Option(it) else Option()}

inline fun <VALUE:Any, OTHER:Any, RETURN:Any> Option<VALUE>.map2(other:Option<OTHER>, block:(VALUE, OTHER)->RETURN):Option<RETURN>
    = if((this is Option.Some) && (other is Option.Some)) Option(block(value, other.value)) else Option()
inline fun <VALUE:Any, SECOND:Any, THIRD:Any, RETURN:Any> Option<VALUE>.map3(second:Option<SECOND>, third:Option<THIRD>, block:(VALUE, SECOND, THIRD)->RETURN):Option<RETURN>
    = if((this is Option.Some) && (second is Option.Some) && (third is Option.Some)) Option(block(value, second.value, third.value)) else Option()
inline fun <VALUE:Any, OTHER:Any, RETURN:Any> Option<VALUE>.map2F(other:Option<OTHER>, block:(VALUE, OTHER)->RETURN):Option<RETURN>
    = flatMap{v1->other.map {v2->block(v1, v2)}}
inline fun <VALUE:Any, SECOND:Any, THIRD:Any, RETURN:Any> Option<VALUE>.map3F(second:Option<SECOND>, third:Option<THIRD>, block:(VALUE, SECOND, THIRD)->RETURN):Option<RETURN>
    = flatMap{v1->second.flatMap {v2->third.map {v3->block(v1, v2, v3)}}}

fun <VALUE:Any> List<Option<VALUE>>._sequenceOption(acc:Option<List<VALUE>>):Option<List<VALUE>>
    = when(this){
    is List.Nil -> acc
    is Cons -> when(val v = _head){
        is Option.None->Option()
        is Option.Some->v.map2(_tail._sequenceOption(acc), ::Cons)
    }
}
inline fun <VALUE:Any> List<Option<VALUE>>.sequenceOption():Option<List<VALUE>> = _sequenceOption(Option(List()))

   //= reverse().fold(Option(List())){acc, it->it.map2(acc, ::Cons)}
inline fun <VALUE:Any> List<Option<VALUE>>.sequenceOptionT():Option<List<VALUE>>
    = traverseOption{it}
fun <VALUE:Any, OTHER:Any> List<VALUE>.traverseOption(block:(VALUE)->Option<OTHER>):Option<List<OTHER>>
    = when(this){
        is List.Nil -> Option(List())
        is Cons ->when(val v = block(_head)){
            is Option.None->Option()
            is Option.Some->v.map2(_tail.traverseOption(block), ::Cons)
        }
    }
fun <VALUE:Any, OTHER:Any, RETURN:Any> Option<VALUE>.map2bind(other:Option<OTHER>, block:(VALUE, OTHER)->RETURN):Option<RETURN>
 = Option.catches{ block(getOrThrow(), other.getOrThrow()) }


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
fun List<Double>.varianceK():Option<Double> = average().flatMap {
    map { item -> (item - it).pow(2) }.average()
}