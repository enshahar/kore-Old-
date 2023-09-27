@file:Suppress("NOTHING_TO_INLINE")

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
fun <ITEM:Any> FStream<ITEM>.takeWhile(block:(ITEM)->Boolean): FStream<ITEM>
= foldRight({FStream()}){it, acc->if(block(it)) FStream({it}, acc) else FStream()}
//= if(this is Stream.Cons && block(_head())) Stream(_head) { _tail().takeWhile(block) } else Stream()
//= when(this){
//    is Stream.Empty -> Stream()
//    is Stream.Cons->if(block(_head())) Stream(_head) { _tail().takeWhile(block) } else Stream()
//}
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

