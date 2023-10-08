@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package kore.fd

import kore.fd.FList.Cons
import kore.fd.FList.Nil

sealed class FList<out ITEM:Any>{
    data object Nil:FList<Nothing>()
    data class Cons<out ITEM:Any>@PublishedApi internal constructor(@PublishedApi internal val head:ITEM, @PublishedApi internal val tail: FList<ITEM>):
        FList<ITEM>()
    companion object{
        inline operator fun <ITEM:Any> invoke(vararg items:ITEM): FList<ITEM> = items.foldRight(invoke(), ::Cons)
        inline operator fun <ITEM:Any> invoke(): FList<ITEM> = Nil
    }
}
inline val <ITEM:Any> FList<ITEM>.size:Int get() = this._size(0)
@PublishedApi internal tailrec fun <ITEM:Any> FList<ITEM>._size(acc:Int):Int
= when(this) {
    is Cons -> tail._size(acc + 1)
    is Nil -> acc
}
inline fun <ITEM:Any> FList<ITEM>.toList():List<ITEM>
= fold(listOf()){acc, it->acc + it}
inline fun <ITEM:Any> FList<ITEM>.setHead(item:ITEM):FList<ITEM>
= when(this){
    is Cons -> Cons(item, tail)
    is Nil -> this
}
inline fun <ITEM:Any> FList<ITEM>.addFirst(item:ITEM):FList<ITEM>
= when(this){
    is Cons -> Cons(item, this)
    is Nil -> this
}
//** base-----------------------------------------------------------------*/
tailrec fun <ITEM:Any, ACC:Any> FList<ITEM>.fold(acc:ACC, block:(ACC, ITEM)->ACC):ACC
= when(this){
    is Cons -> tail.fold(block(acc, head), block)
    is Nil -> acc
}
inline fun <ITEM:Any, ACC:Any> FList<ITEM>.foldIndexed(base:ACC, noinline block:(Int, ACC, ITEM)->ACC):ACC
= fold(base to 0){(acc, index), it->block(index, acc, it) to index + 1}.first
inline fun <ITEM:Any> FList<ITEM>.reverse(): FList<ITEM>
= fold(FList()){acc, it->Cons(it, acc)}
inline fun <ITEM:Any, ACC:Any> FList<ITEM>.foldRight(base:ACC, crossinline block:(ITEM, ACC)->ACC):ACC
= reverse().fold(base){ acc, it->block(it, acc)}
fun <ITEM:Any, ACC:Any> FList<ITEM>.foldRightIndexed(base:ACC, block:(Int, ITEM, ACC)->ACC):ACC
= reverse().foldIndexed(base){index, acc, it->block(index, it, acc)}
inline fun <ITEM:Any, OTHER:Any> FList<ITEM>.map(crossinline block:(ITEM)->OTHER): FList<OTHER>
= foldRight(FList()){ it, acc->Cons(block(it), acc)}
inline fun <ITEM:Any, OTHER:Any> FList<ITEM>.flatMap(noinline block:(ITEM)-> FList<OTHER>):FList<OTHER>
= foldRight(FList()){it, acc->
    when(val v = block(it)){
        is Cons ->v.foldRight(acc, ::Cons)
        is Nil ->acc
    }
}
fun <ITEM:Any> FList<FList<ITEM>>.flatten(): FList<ITEM>
= foldRight(FList()){it, acc->it.foldRight(acc, ::Cons)}
//** append-----------------------------------------------------------------*/
inline fun <ITEM:Any> FList<ITEM>.append(list: FList<ITEM> = FList()): FList<ITEM>
= foldRight(list, ::Cons)
inline fun <ITEM:Any> FList<ITEM>.copy():FList<ITEM>
= append()
inline operator fun <ITEM:Any> FList<ITEM>.plus(list:FList<ITEM>):FList<ITEM>
= append(list)
//** drop----------------------------------------------------------*/
tailrec fun <ITEM:Any> FList<ITEM>.drop(n:Int = 1): FList<ITEM>
= if(n > 0 && this is Cons) tail.drop(n - 1) else this
tailrec fun <ITEM:Any> FList<ITEM>.dropWhile(block:(ITEM)->Boolean): FList<ITEM>
= if(this is Cons && block(head)) tail.dropWhile(block) else this
@PublishedApi internal tailrec fun <ITEM:Any> FList<ITEM>._dropWhileIndexed(index:Int, block:(Int, ITEM)->Boolean): FList<ITEM>
= if(this is Cons && block(index, head)) tail._dropWhileIndexed(index + 1, block) else this
inline fun <ITEM:Any> FList<ITEM>.dropWhileIndexed(noinline block:(Int, ITEM)->Boolean):FList<ITEM>
= _dropWhileIndexed(0, block)
//** dropLast----------------------------------------------------------*/
@PublishedApi internal tailrec fun <ITEM:Any> FList<ITEM>._dropLastWhileIndexed(index:Int, block:(Int, ITEM)->Boolean):FList<ITEM>
= when(this){
    is Cons -> if(!block(index, head)) reverse() else tail._dropLastWhileIndexed(index + 1, block)
    is Nil -> reverse()
}
inline fun <ITEM:Any> FList<ITEM>.dropLastWhileIndexed(noinline block:(Int, ITEM)->Boolean):FList<ITEM>
= reverse()._dropLastWhileIndexed(0, block)
inline fun <ITEM:Any> FList<ITEM>.dropLastWhile(noinline block:(ITEM)->Boolean):FList<ITEM>
= reverse()._dropLastWhileIndexed(0){_,it->block(it)}
fun <ITEM:Any> FList<ITEM>.dropLast(n:Int = 1):FList<ITEM>
= reverse()._dropLastWhileIndexed(0){index,_->index < n}
//** utils-----------------------------------------------------------------*/
fun <ITEM:Any> FList<ITEM>.filter(block:(ITEM)->Boolean): FList<ITEM>
= foldRight(FList()){it, acc->if(block(it)) Cons(it, acc) else acc}
tailrec fun <ITEM:Any> FList<ITEM>.sliceFrom(item:ITEM): FList<ITEM>
= if(this is Cons && head != item) tail.sliceFrom(item) else this
tailrec fun <ITEM:Any> FList<ITEM>.slice(from:Int): FList<ITEM>
= if(this is Cons && from > 0) tail.slice(from - 1) else this
inline fun <ITEM:Any> FList<ITEM>.slice(from:Int, to:Int): FList<ITEM>
= slice(from).dropLast(to - from)
tailrec fun <ITEM:Any> FList<ITEM>.startsWith(target:FList<ITEM>):Boolean
= when(this) {
    is Cons ->{
        when(target){
            is Cons -> if(head == target.head) tail.startsWith(target.tail) else false
            is Nil -> true
        }
    }
    is Nil -> target is Nil
}
tailrec operator fun <ITEM:Any> FList<ITEM>.contains(target:ITEM):Boolean
= when(this) {
    is Cons -> if(head == target) true else target in tail
    is Nil -> false
}
tailrec operator fun <ITEM:Any> FList<ITEM>.contains(target: FList<ITEM>):Boolean
= when(this) {
    is Cons -> when (target) {
        is Cons -> if(startsWith(target)) true else target in tail
        is Nil -> false
    }
    is Nil -> target is Nil
}
inline fun <ITEM:Any, OTHER:Any, RESULT:Any> FList<ITEM>.zipWith(other: FList<OTHER>, noinline block:(ITEM, OTHER)->RESULT): FList<RESULT>
= fold(FList<RESULT>() to other){(acc, other), it->
    when(other){
        is Cons -> Cons(block(it, other.head), acc) to other.tail
        is Nil -> acc to other
    }
}.first.reverse()