@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package kore.wrap

import kore.wrap.WList.Cons
import kore.wrap.WList.Nil

sealed class WList<out ITEM:Any>{
    data object Nil: WList<Nothing>()
    data class Cons<out ITEM:Any> @PublishedApi internal constructor(
            @PublishedApi internal val head:ITEM,
            @PublishedApi internal val tail:Wrap<WList<ITEM>>
    ): WList<ITEM>()
    companion object{
        inline operator fun <ITEM:Any> invoke(vararg items:ITEM): WList<ITEM>
        = items.foldRight(invoke()){ item, acc ->Cons(item, W{acc})}
        inline operator fun <ITEM:Any> invoke(): WList<ITEM> = Nil
    }
    val size:Int get() = when(this){
        is Cons ->when(val t = tail()!!){
            is Cons ->t.size + 1
            is Nil -> 1
        }
        is Nil -> 0
    }
}
inline fun <ITEM:Any> WList<ITEM>.toList():List<ITEM> = fold(listOf()){ acc, it->acc + it}
inline fun <ITEM:Any> WList<ITEM>.setHead(item:ITEM): WList<ITEM> = when(this){
    is Cons -> Cons(item, tail)
    is Nil -> this
}
inline fun <ITEM:Any> WList<ITEM>.setHeadW(item:Wrap<ITEM>): WList<ITEM> = when(this){
    is Cons -> item()?.let{Cons(it, tail)} ?: WList()
    is Nil -> this
}
inline fun <ITEM:Any> WList<ITEM>.addFirst(item:ITEM): WList<ITEM> = when(this){
    is Cons -> Cons(item, W{this})
    is Nil -> this
}
inline fun <ITEM:Any> WList<ITEM>.addFirstW(item:Wrap<ITEM>): WList<ITEM> = when(this){
    is Cons -> item()?.let{Cons(it, W{this})} ?: WList()
    is Nil -> this
}
//** base-----------------------------------------------------------------*/
tailrec fun <ITEM:Any, ACC:Any> WList<ITEM>.foldW(acc:Wrap<ACC>, block:(Wrap<ACC>, ITEM)->Wrap<ACC>):Wrap<ACC>{
    return when(this){
        is Cons-> {
            val tailV = tail.getOrFailEffect{return W(it)}
            tailV.foldW(block(acc, head).failEffect{return W(it)}, block)
        }
        is Nil-> acc
    }
}
tailrec fun <ITEM:Any, ACC:Any> WList<ITEM>.fold(acc:ACC, block:(ACC, ITEM)->ACC):ACC{
    return when(this){
        is Cons-> {
            val tailV = tail.getOrFailEffect{throw it}
            tailV.fold(block(acc, head), block)
        }
        is Nil-> acc
    }
}
inline fun <ITEM:Any> WList<ITEM>.reverseW(): Wrap<WList<ITEM>>
= fold(W{WList()}){ acc, it->W{Cons(it, acc)}}
inline fun <ITEM:Any> WList<ITEM>.reverse(): WList<ITEM>
= reverseW().getOrFailEffect{throw it}
//@PublishedApi internal tailrec fun <ITEM:Any, ACC:Any> WList<ITEM>._foldIndexed(index:Int, acc:Wrap<ACC>, block:(Int, Wrap<ACC>, Wrap<ITEM>)->Wrap<ACC>):Wrap<ACC>
//= when(this){
//    is Cons -> tail()._foldIndexed(index + 1, block(index, acc, head), block)
//    is Nil -> acc
//}
//inline fun <ITEM:Any, ACC:Any> WList<ITEM>.foldIndexed(base:Wrap<ACC>, noinline block:(Int, Wrap<ACC>, Wrap<ITEM>)->Wrap<ACC>):Wrap<ACC>
//= _foldIndexed(0, base, block)

//inline fun <ITEM:Any, ACC:Any> WList<ITEM>.foldRight(base:Wrap<ACC>, crossinline block:(Wrap<ITEM>, Wrap<ACC>)->Wrap<ACC>):Wrap<ACC>
//= reverse()().fold(base){ acc, it->block(it, acc)}
//fun <ITEM:Any, ACC:Any> WList<ITEM>.foldRightIndexed(base:Wrap<ACC>, block:(Int, Wrap<ITEM>, Wrap<ACC>)->Wrap<ACC>):Wrap<ACC>
//= reverse()()._foldIndexed(0, base){index, acc, it->block(index, it, acc)}
//inline fun <ITEM:Any, OTHER:Any> WList<ITEM>.map(crossinline block:(ITEM)->OTHER): Wrap<WList<OTHER>>
//= foldRight(W{WList()}){ it, acc->W{Cons(FV(block(it())), acc)}}
//inline fun <ITEM:Any, OTHER:Wrap<OTHER>> WList<ITEM>.flatMap(noinline block:(ITEM)-> WList<OTHER>):Wrap<WList<OTHER>>
//= foldRight(W{WList()}){ it, acc->
//    when(val v = block(it())){
//        is Cons ->v.foldRight(acc){item,acc2->W{Cons(item, acc2)}}
//        is Nil ->acc
//    }
//}
//fun <ITEM:Any> WList<WList<ITEM>>.flatten(): Wrap<WList<ITEM>>
//= foldRight(W{WList()}){ it, acc->it().foldRight(acc){ it2, acc2->W{Cons(it2, acc2)}}
////** append-----------------------------------------------------------------*/
//fun <ITEM:Any> WList<ITEM>.append(list: Wrap<WList<ITEM>> = W{WList()}): WList<ITEM> = foldRight(list){ it, acc->W{Cons(it, acc)}}()
//fun <ITEM:Any> WList<ITEM>.copy():WList<ITEM> = append()
//operator fun <ITEM:Any> WList<ITEM>.plus(list:WList<ITEM>):WList<ITEM> = append(W{list})
////** drop----------------------------------------------------------*/
//tailrec fun <ITEM:Any> WList<ITEM>._dropWhileIndexed(index:Int, block:(Int, Wrap<ITEM>)->Boolean): WList<ITEM>
//= if(this is Cons && block(index, head)) tail()._dropWhileIndexed(index + 1, block) else this
//fun <ITEM:Any> WList<ITEM>.dropWhileIndexed(block:(Int, Wrap<ITEM>)->Boolean):WList<ITEM> = _dropWhileIndexed(0, block)
//tailrec fun <ITEM:Any> WList<ITEM>.drop(n:Int = 1): WList<ITEM> = if(n > 0 && this is Cons) tail().drop(n - 1) else this
//tailrec fun <ITEM:Any> WList<ITEM>.dropWhile(block:(Wrap<ITEM>)->Boolean): WList<ITEM> = if(this is Cons && block(head)) tail().dropWhile(block) else this
////** dropLast----------------------------------------------------------*/
//tailrec fun <ITEM:Any> WList<ITEM>._dropLastWhileIndexed(index:Int, block:(Int, Wrap<ITEM>)->Boolean):Wrap<WList<ITEM>>
//= when(this){
//    is Cons -> if(!block(index, head)) reverse() else tail()._dropLastWhileIndexed(index + 1, block)
//    is Nil -> reverse()
//}
//fun <ITEM:Any> WList<ITEM>.dropLastWhileIndexed(block:(Int, Wrap<ITEM>)->Boolean):WList<ITEM> = reverse()()._dropLastWhileIndexed(0, block)()
//fun <ITEM:Any> WList<ITEM>.dropLastWhile(block:(Wrap<ITEM>)->Boolean):WList<ITEM> = reverse()()._dropLastWhileIndexed(0){ _, it->block(it)}()
//fun <ITEM:Any> WList<ITEM>.dropLast(n:Int = 1):WList<ITEM> = reverse()()._dropLastWhileIndexed(0){ index, _->index < n}()
////** utils-----------------------------------------------------------------*/
//fun <ITEM:Any> WList<ITEM>.filter(block:(Wrap<ITEM>)->Boolean):Wrap<WList<ITEM>>
//= foldRight(W{WList()}){ it, acc->if(block(it)) W{Cons(it, acc)} else acc}
//tailrec fun <ITEM:Any> WList<ITEM>.sliceFrom(item:ITEM): WList<ITEM> = if(this is Cons && head != item) tail.sliceFrom(item) else this
//tailrec fun <ITEM:Any> WList<ITEM>.slice(from:Int): WList<ITEM> = if(this is Cons && from > 0) tail.slice(from - 1) else this
//inline fun <ITEM:Any> WList<ITEM>.slice(from:Int, to:Int): WList<ITEM> = slice(from).dropLast(to - from)
//tailrec fun <ITEM:Any> WList<ITEM>.startsWith(target:WList<ITEM>):Boolean = when(this) {
//    is Cons ->{
//        when(target){
//            is Cons -> if(head == target.head) when(target.tail){
//                is Cons-> tail.startsWith(target.tail)
//                is Nil -> true
//            } else false
//            is Nil -> false
//        }
//    }
//    is Nil -> target is Nil
//}
//tailrec operator fun <ITEM:Any> WList<ITEM>.contains(target:ITEM):Boolean = when(this) {
//    is Cons -> if(head == target) true else target in tail
//    is Nil -> false
//}
//tailrec operator fun <ITEM:Any> WList<ITEM>.contains(target: WList<ITEM>):Boolean = when(this) {
//    is Cons -> when (target) {
//        is Cons -> if(startsWith(target)) true else target in tail
//        is Nil -> false
//    }
//    is Nil -> target is Nil
//}
//inline fun <ITEM:Any, OTHER:Any, RESULT:Any> WList<ITEM>.zipWith(other: WList<OTHER>, noinline block:(ITEM, OTHER)->RESULT): WList<RESULT> {
//    if(this is Nil || other is Nil) return WList()
//    val thisSize = size
//    val otherSize = other.size
//    return if(otherSize > thisSize){
//        foldRight(WList<RESULT>() to other.dropLast(otherSize - thisSize).reverse()){ it, (acc, target)->
//            when(target){
//                is Cons -> Cons(block(it, target.head), acc) to target.tail
//                is Nil -> acc to target
//            }
//        }.first
//    } else {
//        other.foldRight(WList<RESULT>() to dropLast(thisSize - otherSize).reverse()){ it, (acc, target)->
//            when(target){
//                is Cons -> Cons(block(target.head, it), acc) to target.tail
//                is Nil -> acc to target
//            }
//        }.first
//    }
//}