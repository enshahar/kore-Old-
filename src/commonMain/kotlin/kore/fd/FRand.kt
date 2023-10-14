@file:Suppress("FunctionName")

package kore.fd

import kotlin.math.absoluteValue

interface FRand{
    data class IntRand internal constructor(val seed:Long):FRand{
        override fun nextInt(): Pair<Int, FRand> {
            val newSeed = (seed * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL
            return (newSeed ushr 16).toInt() to IntRand(newSeed)
        }
    }

    companion object{
        fun intRand(seed: Long):FRand = IntRand(seed)
    }
    fun nextInt():Pair<Int, FRand>
}
fun FRand.nextNonNegative():Pair<Int, FRand>{
    val (r, next) = nextInt()
    return (if(r == Int.MIN_VALUE) r + 1 else r).absoluteValue to next
}
fun FRand.nextDouble():Pair<Double, FRand>{
    val (r, next) = nextNonNegative()
    return r.toDouble() / Int.MAX_VALUE.toDouble() to next
}
fun FRand.nextIntDouble():Pair<Pair<Int, Double>, FRand>{
    val (r1, next1) = nextInt()
    val (r2, next2) = nextDouble()
    return r1 to r2 to next2
}
tailrec fun FRand._nextList(list:FList<Int>, count:Int):Pair<FList<Int>, FRand>
= if(count > 0){
    val (r, rand) = nextInt()
    rand._nextList(list + FList(r), count - 1)
}else list to this
fun FRand.nextList(count:Int):Pair<FList<Int>, FRand> = _nextList(FList(), count)


fun interface FRandState<VALUE:Any> : (FRand)->Pair<VALUE, FRand>{
    companion object{
        operator fun <VALUE:Any> invoke(f:(FRand)->Pair<VALUE, FRand>):FRandState<VALUE> = FRandState{f(it)}
    }
}
val a:FRandState<Int> = FRandState(FRand::nextNonNegative)

fun <VALUE:Any> VALUE.toState(): FRandState<VALUE> = FRandState{this to it}
fun <VALUE:Number, OTHER:Number> FRandState<VALUE>.map(block:(VALUE)->OTHER):FRandState<OTHER> = FRandState{
    val (value, state) =this(it)
    block(value) to state
}
fun FRandState<Int>.nonNegativeEven():FRandState<Int> = FRandState(FRand::nextNonNegative).map { it - (it % 2) }