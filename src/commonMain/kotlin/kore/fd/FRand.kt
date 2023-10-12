@file:Suppress("FunctionName")

package kore.fd

import kotlin.math.absoluteValue

interface FRand<VALUE:Number>{
    data class IntRand internal constructor(val seed:Long):FRand<Int>{
        override fun next(): Pair<Int, FRand<Int>> {
            val newSeed = (seed * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL
            return (newSeed ushr 16).toInt() to IntRand(newSeed)
        }
    }

    companion object{
        fun intRand(seed: Long):FRand<Int> = IntRand(seed)
    }
    fun next():Pair<VALUE, FRand<VALUE>>
}
fun FRand<Int>.nextNonNegative():Pair<Int, FRand<Int>>{
    val (r, next) = next()
    return (if(r == Int.MIN_VALUE) r + 1 else r).absoluteValue to next
}
fun FRand<Int>.nextDouble():Pair<Double, FRand<Int>>{
    val (r, next) = nextNonNegative()
    return r.toDouble() / Int.MAX_VALUE.toDouble() to next
}
fun FRand<Int>.nextIntDouble():Pair<Pair<Int, Double>, FRand<Int>>{
    val (r1, next1) = next()
    val (r2, next2) = nextDouble()
    return r1 to r2 to next2
}
tailrec fun FRand<Int>._nextList(list:FList<Int>, count:Int):Pair<FList<Int>, FRand<Int>>
= if(count > 0){
    val (r, rand) = next()
    rand._nextList(list + FList(r), count - 1)
}else list to this
fun FRand<Int>.nextList(count:Int):Pair<FList<Int>, FRand<Int>> = _nextList(FList(), count)