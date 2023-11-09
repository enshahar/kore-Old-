@file:Suppress("FunctionName")

package kore.fd

import kotlin.math.absoluteValue

fun interface FIntState:(FIntState)->Pair<Int, FIntState>{
    operator fun invoke():Pair<Int, FIntState> = invoke(this)
}
fun interface FGen<VALUE:Any>:(FIntState)->Pair<VALUE, FIntState> {
    companion object{
         operator fun <VALUE:Any> invoke(block:(FIntState)->Pair<VALUE, FIntState>):FGen<VALUE> = FGen{block(it)}
    }
}
data class IntRand(val seed:Long):FIntState{
    override fun invoke(state: FIntState): Pair<Int, FIntState> {
        val newSeed = ((state as IntRand).seed * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL
        return (newSeed ushr 16).toInt() to IntRand(newSeed)
    }
}
fun FIntState.nonNegative():Pair<Int, FIntState>
= invoke().let{(v, state)->
    (if(v == Int.MIN_VALUE) v + 1 else v).absoluteValue to state
}
fun FIntState.double():Pair<Double, FIntState>
= nonNegative().let{ (v, state)->
    (if(v == 0) 0.0 else v.toDouble() / Int.MAX_VALUE.toDouble()) to state
}
fun FIntState.nextIntDouble():Pair<Pair<Int, Double>, FIntState>{
    val (r1, next1) = invoke()
    val (r2, next2) = next1.double()
    return r1 to r2 to next2
}
private tailrec fun FIntState._intList(list:FList<Int>, count:Int):Pair<FList<Int>, FIntState>
= if(count > 0){
    val (r, state) = this()
    state._intList(FList.Cons(r, list), count - 1)
}else list to this
fun FIntState.intList(count:Int):Pair<FList<Int>, FIntState> = _intList(FList(), count)
fun <VALUE:Any> VALUE.toState(): FGen<VALUE> = FGen{this to it}
fun <VALUE:Number, OTHER:Number> FGen<VALUE>.map(block:(VALUE)->OTHER):FGen<OTHER> = FGen{
    val (value, state) = this(it)
    block(value) to state
}
fun FGen<Int>.nonNegativeEven():FGen<Int> = FGen(FIntState::nonNegative).map { it - (it % 2) }