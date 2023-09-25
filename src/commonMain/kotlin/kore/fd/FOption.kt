@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package kore.fd

import kore.fd.FList.Cons
import kore.fd.FOption.None
import kore.fd.FOption.Some
import kotlin.math.pow

sealed class FOption<out VALUE:Any> {
    companion object{
        inline operator fun <VALUE:Any> invoke(): FOption<VALUE> = None
        inline operator fun <VALUE:Any> invoke(value:VALUE): FOption<VALUE> = Some(value)
        fun <VALUE:Any, OTHER:Any> lift(block:(VALUE)->OTHER):(FOption<VALUE>)->FOption<OTHER> = {it.map(block)}
        inline fun <VALUE:Any> catches(block:()->VALUE): FOption<VALUE> = try{
            FOption(block())
        }catch (e:Throwable) {
            None
        }
    }
    data object None: FOption<Nothing>()
    data class  Some<out VALUE:Any>(val value: VALUE): FOption<VALUE>()

    inline fun getOrThrow():VALUE = when(this){
        is None ->throw Throwable()
        is Some ->value
    }
}
inline fun <VALUE:Any, OTHER:Any> FOption<VALUE>.map(block:(VALUE)->OTHER):FOption<OTHER> = when(this){
    is None -> this
    is Some -> FOption(block(value))
}
inline fun <VALUE:Any> FOption<VALUE>.getOrElse(block:()->VALUE):VALUE = when(this){
    is None -> block()
    is Some -> value
}
inline fun <VALUE:Any, OTHER:Any> FOption<VALUE>.flatMap(block:(VALUE)->FOption<OTHER>):FOption<OTHER> = when(this){
    is None ->this
    is Some ->block(value)
}
inline fun <VALUE:Any> FOption<VALUE>.orElse(block:()-> FOption<VALUE>): FOption<VALUE> = when(this){
    is None ->block()
    is Some ->this
}
inline fun <VALUE:Any> FOption<VALUE>.filter(block:(VALUE)->Boolean): FOption<VALUE> = when(this){
    is None ->this
    is Some ->if(block(value)) this else FOption()
}
inline fun <VALUE:Any, OTHER:Any, RETURN:Any> FOption<VALUE>.map2(other: FOption<OTHER>, block:(VALUE, OTHER)->RETURN): FOption<RETURN>
= if((this is Some) && (other is Some)) FOption(block(value, other.value)) else FOption()
inline fun <VALUE:Any, SECOND:Any, THIRD:Any, RETURN:Any> FOption<VALUE>.map3(second: FOption<SECOND>, third: FOption<THIRD>, block:(VALUE, SECOND, THIRD)->RETURN): FOption<RETURN>
= if((this is Some) && (second is Some) && (third is Some)) FOption(block(value, second.value, third.value)) else FOption()
inline fun <VALUE:Any> FList<FOption<VALUE>>.sequence(): FOption<FList<VALUE>>
= foldRight(FOption(FList())){it, acc->it.map2(acc, ::Cons)}
fun <VALUE:Any, OTHER:Any> FList<VALUE>.traverse(block:(VALUE)->FOption<OTHER>):FOption<FList<OTHER>>
= foldRight(FOption(FList())){it, acc->block(it).map2(acc, ::Cons)}
inline fun <VALUE:Any> FList<FOption<VALUE>>.sequenceT(): FOption<FList<VALUE>>
= traverse{it}
//** flatMap base------------------------------------------------*/
inline fun <VALUE:Any, OTHER:Any> FOption<VALUE>.flatMapF(block:(VALUE)-> FOption<OTHER>): FOption<OTHER>
= map(block).getOrElse { FOption() }
inline fun <VALUE:Any> FOption<VALUE>.orElseF(block:()-> FOption<VALUE>): FOption<VALUE>
= map{ FOption(it) }.getOrElse(block)
inline fun <VALUE:Any> FOption<VALUE>.filterF(block:(VALUE)->Boolean): FOption<VALUE>
= flatMap{if(block(it)) FOption(it) else FOption() }
inline fun <VALUE:Any, OTHER:Any, RETURN:Any> FOption<VALUE>.map2F(other: FOption<OTHER>, block:(VALUE, OTHER)->RETURN): FOption<RETURN>
= flatMap{v1->other.map {v2->block(v1, v2)}}
inline fun <VALUE:Any, SECOND:Any, THIRD:Any, RETURN:Any> FOption<VALUE>.map3F(second: FOption<SECOND>, third: FOption<THIRD>, block:(VALUE, SECOND, THIRD)->RETURN): FOption<RETURN>
= flatMap{v1->second.flatMap {v2->third.map {v3->block(v1, v2, v3)}}}




inline fun List<Double>.variance(): FOption<Double> = if(isEmpty()) FOption() else{
    val avg = average()
    FOption(map{(it - avg).pow(2)}.average())
}
fun FList<Double>.average(): FOption<Double> = when(val s = size){
    0 -> FOption()
    else -> FOption(fold(0.0, Double::plus) / s)
}
fun FList<Double>.variance(): FOption<Double> = when(val avg = average()){
    is None ->avg
    is Some ->map{(it - avg.value).pow(2)}.average()
}
fun FList<Double>.varianceK(): FOption<Double> = average().flatMap{
    map { item -> (item - it).pow(2) }.average()
}