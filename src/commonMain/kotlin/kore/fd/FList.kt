@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package kore.fd

import kore.fd.FList.Cons
import kore.fd.FList.Nil

sealed class FList<out ITEM:Any>{
    data object Nil: FList<Nothing>()
    data class Cons<out ITEM:Any>(@PublishedApi internal val head:ITEM, @PublishedApi internal val tail: FList<ITEM>):
        FList<ITEM>()
    companion object{
        inline operator fun <ITEM:Any> invoke(vararg items:ITEM): FList<ITEM> = items.foldRight(invoke(), ::Cons)
        inline operator fun <ITEM:Any> invoke(): FList<ITEM> = Nil
    }
    val size:Int get() = when(this){
        is Cons ->when(tail){
            is Cons ->tail.size + 1
            is Nil -> 1
        }
        is Nil -> 0
    }
    inline fun getHeadOrNull():ITEM? = when(this){
        is Cons -> head
        is Nil -> null
    }

}
inline fun <ITEM:Any> FList<ITEM>.setHead(item:ITEM):FList<ITEM> = when(this){
    is Cons -> Cons(item, tail)
    is Nil -> FList()
}
inline fun <ITEM:Any> FList<ITEM>.addFirst(item:ITEM):FList<ITEM> = when(this){
    is Cons -> Cons(item, this)
    is Nil -> FList()
}
//** base-----------------------------------------------------------------*/
tailrec fun <ITEM:Any, ACC:Any> FList<ITEM>.fold(acc:ACC, block:(ACC, ITEM)->ACC):ACC = when(this){
    is Cons -> tail.fold(block(acc, head), block)
    is Nil -> acc
}
inline fun <ITEM:Any> FList<ITEM>.reverse(): FList<ITEM> = fold(FList()){acc, it->Cons(it, acc)}
inline fun <ITEM:Any, ACC:Any> FList<ITEM>.foldRight(base:ACC, crossinline block:(ITEM, ACC)->ACC):ACC = reverse().fold(base){ acc, it->block(it, acc)}
inline fun <ITEM:Any, OTHER:Any> FList<ITEM>.map(crossinline block:(ITEM)->OTHER): FList<OTHER> = foldRight(FList()){ it, acc->Cons(block(it), acc)}
@PublishedApi internal tailrec fun <ITEM:Any, OTHER:Any> FList<ITEM>._flatMap(acc:FList<OTHER>, block:(ITEM)-> FList<OTHER>):FList<OTHER> = when(this){
    is Cons -> when(val v = block(head)){
        is Cons ->tail._flatMap(acc + v, block)
        is Nil ->tail._flatMap(acc, block)
    }
    is Nil -> acc
}
inline fun <ITEM:Any, OTHER:Any> FList<ITEM>.flatMap(noinline block:(ITEM)-> FList<OTHER>): FList<OTHER> = _flatMap(FList(), block)
fun <ITEM:Any> FList<FList<ITEM>>.flatten(): FList<ITEM> = reverse().fold<FList<ITEM>, FList<ITEM>>(FList()){acc, it->
    it.reverse().fold(acc){acc2, it2->Cons(it2, acc2)}
}
//** append-----------------------------------------------------------------*/
inline fun <ITEM:Any> FList<ITEM>.append(list: FList<ITEM> = FList.invoke()): FList<ITEM> = foldRight(list, ::Cons)
inline fun <ITEM:Any> FList<ITEM>.copy():FList<ITEM> = append()
inline operator fun <ITEM:Any> FList<ITEM>.plus(list:FList<ITEM>):FList<ITEM> = append(list)
//** drop----------------------------------------------------------*/
@PublishedApi internal tailrec fun <ITEM:Any> FList<ITEM>._dropWhileIndexed(index:Int, block:(Int, ITEM)->Boolean): FList<ITEM>
= if(this is Cons && block(index, head)) tail._dropWhileIndexed(index + 1, block) else this
inline fun <ITEM:Any> FList<ITEM>.dropWhileIndexed(noinline block:(Int, ITEM)->Boolean):FList<ITEM> = _dropWhileIndexed(0, block)
tailrec fun <ITEM:Any> FList<ITEM>.drop(n:Int = 1): FList<ITEM> = if(n > 0 && this is Cons) tail.drop(n - 1) else this
tailrec fun <ITEM:Any> FList<ITEM>.dropWhile(block:(ITEM)->Boolean): FList<ITEM> = if(this is Cons && block(head)) tail.dropWhile(block) else this
//** dropLast----------------------------------------------------------*/

@PublishedApi internal tailrec fun <ITEM:Any> FList<ITEM>._dropLastWhileIndexed(index:Int, block:(Int, ITEM)->Boolean):FList<ITEM>
= when(this){
    is Cons -> if(!block(index, head)) reverse() else tail._dropLastWhileIndexed(index + 1, block)
    is Nil -> reverse()
}
inline fun <ITEM:Any> FList<ITEM>.dropLastWhileIndexed(noinline block:(Int, ITEM)->Boolean):FList<ITEM> = reverse()._dropLastWhileIndexed(0, block)
inline fun <ITEM:Any> FList<ITEM>.dropLastWhile(noinline block:(ITEM)->Boolean):FList<ITEM> = reverse()._dropLastWhileIndexed(0){_,it->block(it)}
fun <ITEM:Any> FList<ITEM>.dropLast(n:Int = 1):FList<ITEM> = reverse()._dropLastWhileIndexed(0){index,_->index<n}

///**-----------------------------------------------------------------*/
//fun <ITEM:Any> FList<ITEM>.filter(block:(ITEM)->Boolean): FList<ITEM> = foldRight(FList()){it, acc->
//    if(block(it)) Cons(it, acc) else acc
//}
//
//
//@PublishedApi internal tailrec fun <ITEM:Any, ACC:Any> FList<ITEM>._foldIndexed(index:Int, base:ACC, block:(Int, ACC, ITEM)->ACC):ACC = when(this){
//    is Nil -> base
//    is Cons -> _tail._foldIndexed(index + 1, block(index, base, _head), block)
//}
//fun <ITEM:Any, ACC:Any> FList<ITEM>.foldIndexed(base:ACC, block:(Int, ACC, ITEM)->ACC):ACC = _foldIndexed(0, base, block)
//
//
//
//@PublishedApi internal tailrec fun <ITEM:Any, ACC:Any> FList<ITEM>._foldRightIndexed(index:Int, base:ACC, block:(Int, ITEM, ACC)->ACC):ACC = when(this){
//    is Nil -> base
//    is Cons -> _tail._foldRightIndexed(index + 1, block(index, _head, base), block)
//}
//fun <ITEM:Any, ACC:Any> FList<ITEM>.foldRightIndexed(base:ACC, block:(Int, ITEM, ACC)->ACC):ACC = reverse()._foldRightIndexed(0, base, block)
//
//
//@PublishedApi internal fun <ITEM:Any, ACC:Any> FList<ITEM>._foldRightIndexed2(index:Int, base:ACC, block:(Int, ITEM, ACC)->ACC):ACC = when(this){
//    is Nil -> base
//    is Cons -> block(index, _head, _tail._foldRightIndexed2(index - 1, base, block))
//}
//fun <ITEM:Any, ACC:Any> FList<ITEM>.foldRightIndexed2(base:ACC, block:(Int, ITEM, ACC)->ACC):ACC = _foldRightIndexed2(size - 1, base, block)
//
//inline val <ITEM:Any> FList<ITEM>.size:Int get() = fold(0){ acc, _->acc + 1}
//fun <ITEM:Any> FList<ITEM>.dropLast2(n:Int = 1): FList<ITEM> = foldRightIndexed(FList.invoke()){ index, it, acc->
//    if(index >= n) Cons(it, acc) else FList.invoke()
//}
//inline fun FList<Int>.sum():Int = fold(0){ acc, it-> acc + it}
//inline fun FList<Long>.sum():Long = fold(0L){ acc, it-> acc + it}
//inline fun FList<Float>.sum():Float = fold(0.0f){ acc, it-> acc + it}
//inline fun FList<Double>.sum():Double = fold(0.0){ acc, it-> acc + it}
//
//inline fun FList<Int>.product():Int = fold(0){ acc, it-> acc * it}
//inline fun FList<Long>.product():Long = fold(0L){ acc, it-> acc * it}
//inline fun FList<Float>.product():Float = fold(0.0f){ acc, it-> acc * it}
//inline fun FList<Double>.product():Double = fold(0.0){ acc, it-> acc * it}
//
//
//

//@PublishedApi internal tailrec fun <ITEM:Any, OTHER:Any, RESULT:Any> FList<ITEM>._zipWithFold(other: FList<OTHER>, base: FList<RESULT>, block:(ITEM, OTHER)->RESULT): FList<RESULT> = when(this){
//    is Nil -> base
//    is Cons -> when(other){
//        is Nil -> base
//        is Cons -> _tail._zipWithFold(other.tail, Cons(block(_head, other._head), base), block)
//    }
//}
//fun <ITEM:Any, OTHER:Any, RESULT:Any> FList<ITEM>.zipWith(other: FList<OTHER>, block:(ITEM, OTHER)->RESULT): FList<RESULT> {
//     return if(other.size > size){
//         reverse()._zipWithFold(other.dropLast(other.size - size).reverse(), FList.invoke(), block)
//     }else{
//         other.reverse()._zipWithFold(dropLast(size - other.size).reverse(), FList.invoke(), { a, b->block(b, a)})
//     }
//}
//tailrec fun <ITEM:Any> FList<ITEM>.sliceFrom(item:ITEM): FList<ITEM> = when(this){
//    is Nil -> Nil
//    is Cons -> if(_head == item) this else _tail.sliceFrom(item)
//}
//tailrec fun <ITEM:Any> FList<ITEM>.startsWith(target: FList<ITEM>):Boolean = when(this) {
//    is Nil -> true
//    is Cons ->if(target is Cons && _head == target._head) _tail.startsWith(target._tail) else false
////    when(target){
////        is List.Nil -> false
////        is List.Cons -> if(_head != target._head) false else _tail.isSameItems(target._tail)
////    }
//}
//fun <ITEM:Any> FList<ITEM>.hasSubSequence(sub: FList<ITEM>):Boolean = when(this) {
//    is Nil -> sub is Nil
//    is Cons -> when (sub) {
//        is Nil -> true
//        is Cons -> when (val subList = sliceFrom(sub._head)) {
//            is Nil -> false
//            is Cons -> sub.startsWith(subList)
//        }
//    }
//}
