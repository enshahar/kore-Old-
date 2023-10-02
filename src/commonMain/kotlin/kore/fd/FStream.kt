@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package kore.fd

import kore.fd.FStream.Cons
import kore.fd.FStream.Empty

sealed class FStream<out ITEM:Any> {
    companion object{
        operator fun <ITEM:Any> invoke(head:()->ITEM, tail:()-> FStream<ITEM>): FStream<ITEM> {
            val h by lazy(head)
            val t by lazy(tail)
            return Cons({h}, {t})
        }
        operator fun <ITEM:Any> invoke(): FStream<ITEM> = Empty
        operator fun <ITEM:Any> invoke(vararg items:ITEM): FStream<ITEM> = items.foldRight(invoke()){ it, acc ->
            FStream({it}, {acc})
        }
        fun <ITEM:Any> constant(item:ITEM):FStream<ITEM> = FStream({item}, { constant(item) })
        fun increaseFrom(item:Int):FStream<Int> = FStream({item}, { increaseFrom(item + 1) })
        private fun _fib(prevprev:Int, prev:Int):FStream<Int> = FStream({prevprev + prev}, { _fib(prev, prevprev + prev) })
        fun fib():FStream<Int> = FStream({0}, { FStream({1}, { _fib(0, 1)}) })
        fun <ITEM:Any, REF:Any> unfold(ref:REF, block:(REF)-> FOption<Pair<ITEM, REF>>):FStream<ITEM> = when(val v = block(ref)){
            is FOption.None -> invoke()
            is FOption.Some -> v.value.let { (item, ref)->invoke({item}, {unfold(ref, block)}) }
        }
        fun from2(item:Int):FStream<Int> = unfold(item){FOption(it to it + 1)}
        fun constant2(item:Int):FStream<Int> = unfold(item){FOption(it to it)}
        fun fib2():FStream<Int> = FStream({0}, {FStream({1}, {unfold(0 to 1){(prevprev, prev)->FOption(prevprev + prev to (prev to prevprev + prev))}})})
    }
    data object Empty: FStream<Nothing>()
    data class Cons<out ITEM:Any>(val head:()->ITEM, val tail:()-> FStream<ITEM>): FStream<ITEM>()
}
//** base-----------------------------------------------------------------*/
tailrec fun <ITEM:Any, ACC:Any> FStream<ITEM>.fold(acc:()->ACC, block:(()->ACC, ITEM)->ACC):ACC = when(this){
    is Cons -> tail().fold({block(acc, head())}, block)
    is Empty -> acc()
}
inline fun <ITEM:Any> FStream<ITEM>.reverse(): FStream<ITEM>
= fold({FStream()}){acc, it-> Cons({it}, acc) }
fun <ITEM:Any,OTHER:Any> FStream<ITEM>.foldRight(emptyBlock:()->OTHER, consBlock:(ITEM, ()->OTHER)->OTHER):OTHER
= reverse().fold(emptyBlock){acc, it->consBlock(it, acc)}
//= when(this){
//    is Empty -> emptyBlock()
//    is Cons ->consBlock(head()){tail().foldRight(emptyBlock, consBlock)}
//}
fun <ITEM:Any, OTHER:Any> FStream<ITEM>.map(block:(ITEM)->OTHER): FStream<OTHER>
= foldRight({FStream()}){it, acc-> FStream({block(it)}, acc)}
fun <ITEM:Any, OTHER:Any> FStream<ITEM>.map2(block:(ITEM)->OTHER): FStream<OTHER>
= FStream.unfold(this){
    when(it){
        is Empty -> FOption()
        is Cons -> FOption(block(it.head()) to it.tail())
    }
}
inline fun <ITEM:Any, OTHER:Any> FStream<ITEM>.flatMap(noinline block:(ITEM)-> FStream<OTHER>):FStream<OTHER>
= foldRight({FStream()}){it, acc->block(it).append(acc)}
fun <ITEM:Any> FStream<ITEM>.filter(block:(ITEM)->Boolean): FStream<ITEM>
= foldRight({FStream()}){ it, acc->if(block(it)) FStream({it}, acc) else acc()}
//** append-----------------------------------------------------------------*/
fun <ITEM:Any> FStream<ITEM>.append(other:()->FStream<ITEM> = {FStream()}): FStream<ITEM>
= foldRight(other){it, acc-> FStream({it}, acc) }
inline fun <ITEM:Any> FStream<ITEM>.copy():FStream<ITEM> = append()
inline operator fun <ITEM:Any> FStream<ITEM>.plus(stream:FStream<ITEM>):FStream<ITEM> = append({stream})

fun <ITEM:Any> FStream<ITEM>.toList(): List<ITEM>
= fold({listOf()}){acc, it->acc() + it}
fun <ITEM:Any> FStream<ITEM>.toFList(): FList<ITEM>
= foldRight({FList()}){it,acc->FList.Cons(it, acc())}
//= when(this){
//    is Empty -> FList()
//    is Cons -> FList.Cons(head(), tail().toList())
//}
fun <ITEM:Any> FStream<ITEM>.take(n:Int): FStream<ITEM>
= if(this is Cons && n > 0) FStream(head) { tail().take(n - 1) } else FStream()
//= when(this){
//    is Stream.Empty -> Stream()
//    is Stream.Cons->if(n > 0) Stream(_head) { _tail().take(n - 1) } else Stream()
//}
fun <ITEM:Any> FStream<ITEM>.take2(n:Int): FStream<ITEM>
= FStream.unfold(this to n) { (stream, n) ->
    when (stream) {
        is Empty -> FOption()
        is Cons -> if (n > 0) FOption(stream.head() to (stream.tail() to n - 1)) else FOption()
    }
}
fun <ITEM:Any> FStream<ITEM>.takeWhile(block:(ITEM)->Boolean): FStream<ITEM>
= foldRight({FStream()}){it, acc->if(block(it)) FStream({it}, acc) else FStream()}
//= if(this is Stream.Cons && block(_head())) Stream(_head) { _tail().takeWhile(block) } else Stream()
//= when(this){
//    is Stream.Empty -> Stream()
//    is Stream.Cons->if(block(_head())) Stream(_head) { _tail().takeWhile(block) } else Stream()
//}
fun <ITEM:Any> FStream<ITEM>.takeWhile2(block:(ITEM)->Boolean): FStream<ITEM>
= FStream.unfold(this to block) { (stream, block) ->
    when (stream) {
        is Empty -> FOption()
        is Cons -> if (block(stream.head())) FOption(stream.head() to (stream.tail() to block)) else FOption()
    }
}
fun <ITEM:Any, OTHER:Any, RESULT:Any> FStream<ITEM>.zipWith(other:FStream<OTHER>, block:(ITEM, OTHER)->RESULT): FStream<RESULT>
= FStream.unfold(this to other) { (stream, other) ->
    when (stream) {
        is Empty -> FOption()
        is Cons -> when (other) {
            is Empty -> FOption()
            is Cons -> FOption(block(stream.head(), other.head()) to (stream.tail() to other.tail()))
        }
    }
}
fun <ITEM:Any, OTHER:Any> FStream<ITEM>.zipAll(other:FStream<OTHER>): FStream<Pair<FOption<ITEM>, FOption<OTHER>>>
= FStream.unfold(this to other) { (stream, other) ->
    when (stream) {
        is Cons -> when (other) {
            is Empty -> FOption((FOption(stream.head()) to FOption<OTHER>()) to (stream.tail() to FStream()))
            is Cons -> FOption((FOption(stream.head()) to FOption(other.head())) to (stream.tail() to other.tail()))
        }
        is Empty ->when (other) {
            is Cons -> FOption((FOption<ITEM>() to FOption(other.head())) to (FStream<ITEM>() to other.tail()))
            is Empty -> FOption()
        }
    }
}
tailrec fun <ITEM:Any> FStream<ITEM>.startsWith(target:FStream<ITEM>):Boolean = when(this){
    is Cons -> when(target){
        is Cons -> if(head() == target.head()) when(val t = target.tail()){
            is Cons -> tail().startsWith(t)
            is Empty -> true
        } else false
        is Empty -> false
    }
    is Empty -> target is Empty
}
fun <ITEM:Any> FStream<ITEM>.startsWith2(target:FStream<ITEM>):Boolean
= zipAll(target).takeWhile{(a, b)->
    a is FOption.Some && b is FOption.Some
}.all{it.first == it.second}
tailrec fun <ITEM:Any> FStream<ITEM>.drop(n:Int): FStream<ITEM>
= if(this is Cons && n > 0) tail().drop(n - 1) else this
//= when(this){
//    is Empty -> FStream()
//    is Cons ->if(n > 0) tail().drop(n - 1) else this
//}
tailrec operator fun <ITEM:Any> FStream<ITEM>.contains(block:(ITEM)->Boolean):Boolean = when(this){
    is Empty -> false
    is Cons->if(block(head())) true else tail().contains(block)
}
tailrec operator fun <ITEM:Any> FStream<ITEM>.contains(item:ITEM):Boolean
= when(this){
    is Empty -> false
    is Cons->if(item == head()) true else tail().contains(item)
}
fun <ITEM:Any> FStream<ITEM>.any(block:(ITEM)->Boolean):Boolean
= foldRight({false}){it, acc->block(it) || acc()}
fun <ITEM:Any> FStream<ITEM>.all(block:(ITEM)->Boolean):Boolean
= foldRight({true}){it, acc->block(it) && acc()}
val <ITEM:Any> FStream<ITEM>.headOption: FOption<ITEM> get()
= foldRight({ FOption() }){ it, _-> FOption(it) }
//= when(this){
//    is Stream.Empty->Option()
//    is Stream.Cons->Option(_head())
//}
fun <ITEM:Any, ACC:Any> FStream<ITEM>.scanRight(acc:ACC, block:(ITEM, ACC)->ACC):FStream<ACC>
= FStream.unfold(this to true){(stream, isAdd)->
    when(stream){
        is Empty -> if(isAdd) FOption(acc to (stream to false)) else FOption()
        is Cons -> FOption(stream.foldRight({acc}){it, acc->block(it, acc())} to (stream.tail() to true))
    }
}
fun <ITEM:Any> FStream<ITEM>.tails():FStream<FStream<ITEM>>
= FStream.unfold(this to true){(stream, isAdd)->
    when(stream){
        is Empty -> if(isAdd) FOption(stream to (stream to false)) else FOption()
        is Cons -> FOption(stream to (stream.tail() to true))
    }
}
