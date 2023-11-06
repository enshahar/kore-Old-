@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package kore.fd

import kore.fd.FStream.Cons
import kore.fd.FStream.Empty

fun <VALUE:Any> lazyIf(
    cond:()->Boolean,
    onTrue:()->VALUE,
    onFalse:()->VALUE
):()->VALUE
= {if(cond()) onTrue() else onFalse()}

fun maybeTwice2(b:Boolean, i:()->Int):Int{
    val j by lazy(i)
    return if(b) j + j else 0
}
fun maybeTwice3(b:Boolean, i:()->Int):Int{
    val j = i()
    return if(b) j + j else 0
}
sealed class FStream<out ITEM:Any> {
    data object Empty:FStream<Nothing>()
    class Cons<out ITEM:Any>@PublishedApi internal constructor(h:()->ITEM, t:()->FStream<ITEM>):FStream<ITEM>(){
        private var memoHead:@UnsafeVariance ITEM? = null
        val head:()->ITEM = {memoHead ?: h().also { memoHead = it }}
        private var memoTail:FStream<@UnsafeVariance ITEM>? = null
        val tail:()->FStream<ITEM> = {memoTail ?: t().also { memoTail = it }}
    }
    companion object{
        @PublishedApi internal val emptyTail:()->FStream<Nothing> = {Empty}
        inline operator fun <ITEM:Any> invoke(noinline head:()->ITEM):FStream<ITEM>
        = Cons(head, emptyTail)
        inline operator fun <ITEM:Any> invoke(noinline head:()->ITEM, noinline tail:()-> FStream<ITEM>):FStream<ITEM>
        = Cons(head, tail)
        inline operator fun <ITEM:Any> invoke(): FStream<ITEM> = Empty
        operator fun <ITEM:Any> invoke(vararg items:ITEM): FStream<ITEM>
        = unfold(items to 0){(items, n)->
            if(items.isEmpty() || items.size <= n) invoke()
            else invoke{{items[n]} to {items to n + 1}}
        }
        //= invoke({items[0]}){invoke(*items.sliceArray(1..items.size))}
    }
}
//** base-----------------------------------------------------------------*/
fun <ITEM:Any,OTHER:Any> FStream<ITEM>.fold(
    empty:()->OTHER,
    cons:(head:()->ITEM, next:()->OTHER)->OTHER
):OTHER
= if(this is Cons) cons(head){tail().fold(empty, cons)} else empty()
fun <ITEM:Any, STATE:Any> FStream.Companion.unfold(state:STATE, block:(STATE)->FStream<Pair<()->ITEM, ()->STATE>>):FStream<ITEM>
= when(val v = block(state)){
    is Empty -> Empty
    is Cons -> invoke({v.head().first()}){unfold(v.head().second(), block)}
}
fun <ITEM:Any, STATE:Any> FStream.Companion.unfoldOption(
    state:STATE,
    block:(STATE)->FOption<Pair<()->ITEM, ()->STATE>>
):FStream<ITEM>
= when(val v = block(state)){
    is FOption.None -> Empty
    is FOption.Some -> {
        val (item, nextState) = v.value
        invoke(item){unfoldOption(nextState(), block)}
    }
}
//----------------------------------------------------------------------------
fun <ITEM:Any> FStream.Companion.constant(item:ITEM):FStream<ITEM> = FStream({item}){constant(item)}
fun <ITEM:Any> FStream.Companion.constant2(item:ITEM):FStream<ITEM> = unfoldOption(item){FOption({it} to {it})}
fun <ITEM:Any> FStream.Companion.constant3(item:ITEM):FStream<ITEM> = unfold(item){ FStream.invoke { { it } to { it } } }
fun FStream.Companion.increaseFrom(item:Int):FStream<Int> = FStream({item}){increaseFrom(item + 1)}
fun FStream.Companion.increaseFrom2(item:Int):FStream<Int> = unfoldOption(item){FOption({it} to {it + 1})}
fun FStream.Companion.increaseFrom3(item:Int):FStream<Int> = unfold(item){ FStream.invoke { { it } to { it + 1 } } }
private fun _fib(prevprev:Int, prev:Int):FStream<Int> = FStream({prevprev + prev}){ _fib(prev, prevprev + prev)}
fun FStream.Companion.fib():FStream<Int> = FStream({0}){FStream({1}){ _fib(0, 1)}}
fun FStream.Companion.fib2():FStream<Int> = FStream({0}){FStream({1}){unfoldOption(0 to 1){ (prevprev, prev)->FOption({prevprev + prev} to {prev to prevprev + prev})}}}
fun FStream.Companion.fib3():FStream<Int> = FStream({0}){FStream({1}){unfold(0 to 1){ (prevprev, prev)-> FStream.invoke { { prevprev + prev } to { prev to prevprev + prev } } }}}
//----------------------------------------------------------------------------
fun <ITEM:Any> FStream<ITEM>.headOption():FOption<ITEM>
= fold({FOption()}){it, _->FOption(it())}
//= if(this is Cons) FOption(head()) else FOption()
fun <ITEM:Any, OTHER:Any> FStream<ITEM>.map(block:(ITEM)->OTHER):FStream<OTHER>
= fold({FStream()}){head, next-> FStream({block(head())}, next)}
fun <ITEM:Any, OTHER:Any> FStream<ITEM>.mapUnfold(block:(ITEM)->OTHER):FStream<OTHER>
= FStream.unfold(this){
    when(it){
        is Empty -> Empty
        is Cons -> FStream{{block(it.head())} to it.tail}
    }
}
fun <ITEM:Any, OTHER:Any> FStream<ITEM>.mapUnfoldOption(block:(ITEM)->OTHER):FStream<OTHER>
= FStream.unfoldOption(this){
    when(it){
        is Empty -> FOption()
        is Cons -> FOption({block(it.head())} to it.tail)
    }
}
inline fun <ITEM:Any, OTHER:Any> FStream<ITEM>.flatMap(noinline block:(ITEM)->FStream<OTHER>):FStream<OTHER>
= fold({FStream()}){head, next->block(head()).append(next)}
// flatmap도 건너뛰기를 해야해서 구현불가
//inline fun <ITEM:Any, OTHER:Any> FStream<ITEM>.flatMapUnfold(noinline block:(ITEM)->FStream<OTHER>):FStream<OTHER>
//= FStream.unfold(this){
//    when(it){
//        is Empty -> Empty
//        is Cons -> when(val v = block(it.head())){
//            is Empty -> Empty
//            is Cons -> FStream{v.head to it.tail}
//        }
//    }
//}
fun <ITEM:Any> FStream<ITEM>.filter(block:(ITEM)->Boolean):FStream<ITEM>
= fold({FStream()}){head, next->if(block(head())) FStream(head, next) else next()}
//필터는 unfold로 구현불가
//fun <ITEM:Any> FStream<ITEM>.filterUnfold(block:(ITEM)->Boolean):FStream<ITEM>
//= FStream.unfold(this){
//    when(it){
//        is Empty -> Empty
//        is Cons -> if(block(it.head())) FStream{it.head to {it.tail()}} else 건너뛸 상황을 표현할 방법이 없음
//}
//** append-----------------------------------------------------------------*/
fun <ITEM:Any> FStream<ITEM>.append(other:()->FStream<ITEM>):FStream<ITEM>
= fold(other){it, acc-> FStream(it, acc)}
fun <ITEM:Any> FStream<ITEM>.appendUnfold(other:()->FStream<ITEM>):FStream<ITEM>
= FStream.unfold(this to other){(origin, other)->
    when(origin){
        is Empty -> when(val v = other()){
            is Empty -> Empty
            is Cons -> FStream{v.head to {origin to v.tail}}
        }
        is Cons ->FStream{origin.head to {origin.tail() to other}}
    }
}
inline operator fun <ITEM:Any> FStream<ITEM>.plus(stream:FStream<ITEM>):FStream<ITEM> = append{stream}
//** ----------------------------------------
fun <ITEM:Any> FStream<ITEM>.toList():List<ITEM>
= fold({listOf<ITEM>()}){ it, acc-> acc() + it()}.reversed()
fun <ITEM:Any> FStream<ITEM>.toFList():FList<ITEM>
= fold({FList()}){ it, acc->FList.Cons(it(), acc())}
fun <ITEM:Any> FStream<ITEM>.take(n:Int):FStream<ITEM>
= FStream.unfold(this to n){(stream, n)->
    when(stream){
        is Empty -> Empty
        is Cons -> if(n > 0) FStream{stream.head to {stream.tail() to n - 1}} else Empty
    }
}
//= if(this is Cons && n > 0) FStream(head) { tail().take(n - 1) } else FStream()
fun <ITEM:Any> FStream<ITEM>.take2(n:Int):FStream<ITEM>
= FStream.unfoldOption(this to n) { (stream, n)->
    when(stream){
        is Empty -> FOption()
        is Cons -> if (n > 0) FOption(stream.head to {stream.tail() to n - 1}) else FOption()
    }
}
fun <ITEM:Any> FStream<ITEM>.takeWhile(block:(ITEM)->Boolean):FStream<ITEM>
= fold({FStream()}){ it, acc->if(block(it())) FStream(it, acc) else FStream()}
fun <ITEM:Any> FStream<ITEM>.takeWhile2(block:(ITEM)->Boolean): FStream<ITEM>
= FStream.unfoldOption(this) { stream ->
    when (stream) {
        is Empty -> FOption()
        is Cons -> if(block(stream.head())) FOption(stream.head to stream.tail) else FOption()
    }
}
fun <ITEM:Any, OTHER:Any, RESULT:Any> FStream<ITEM>.zipWith(other:FStream<OTHER>, block:(ITEM, OTHER)->RESULT):FStream<RESULT>
= FStream.unfoldOption(this to other){(stream, other)->
    when(stream){
        is Empty -> FOption()
        is Cons -> when(other){
            is Empty -> FOption()
            is Cons -> FOption({block(stream.head(), other.head())} to {stream.tail() to other.tail()})
        }
    }
}
fun <ITEM:Any, OTHER:Any> FStream<ITEM>.zipAll(other:FStream<OTHER>):FStream<Pair<FOption<ITEM>, FOption<OTHER>>>
= FStream.unfoldOption(this to other) { (stream, other) ->
    when (stream) {
        is Cons -> when (other) {
            is Empty -> FOption({FOption(stream.head()) to FOption<OTHER>()} to {stream.tail() to FStream()})
            is Cons -> FOption({FOption(stream.head()) to FOption(other.head())} to {stream.tail() to other.tail()})
        }
        is Empty ->when (other) {
            is Cons -> FOption({FOption<ITEM>() to FOption(other.head())} to {FStream<ITEM>() to other.tail()})
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
= fold({false}){ it, acc->block(it()) || acc()}
fun <ITEM:Any> FStream<ITEM>.all(block:(ITEM)->Boolean):Boolean
= fold({true}){ it, acc->block(it()) && acc()}
val <ITEM:Any> FStream<ITEM>.headOption: FOption<ITEM> get()
= fold({ FOption() }){ it, _-> FOption(it()) }
fun <ITEM:Any, ACC:Any> FStream<ITEM>.scanRight(acc:ACC, block:(ITEM, ACC)->ACC):FStream<ACC>
= FStream.unfoldOption(this to true){ (stream, isAdd)->
    when(stream){
        is Empty -> if(isAdd) FOption({ acc } to {stream to false}) else FOption()
        is Cons -> FOption({stream.fold({acc}){ it, acc->block(it(), acc())}} to {stream.tail() to true})
    }
}
fun <ITEM:Any> FStream<ITEM>.tails():FStream<FStream<ITEM>>
= FStream.unfoldOption(this to true){ (stream, isAdd)->
    when(stream){
        is Empty -> if(isAdd) FOption({ stream } to {stream to false}) else FOption()
        is Cons -> FOption({ stream } to {stream.tail() to true})
    }
}

