@file:Suppress("NOTHING_TO_INLINE")

sealed class List<out ITEM:Any>{
    data object Nil:List<Nothing>()
    data class Cons<out ITEM:Any>(@PublishedApi internal val _head:ITEM, @PublishedApi internal val _tail:List<ITEM>):List<ITEM>()
    companion object{
        private tailrec fun <ITEM:Any> List<ITEM>.of(items: kotlin.collections.List<ITEM>): List<ITEM>
            = if(items.isEmpty()) this else Cons(items.last(), this).of(items.dropLast(1))
        fun <ITEM:Any> of2(vararg items:ITEM):List<ITEM> = Nil.of(items.toList())
        fun <ITEM:Any> of(vararg items:ITEM):List<ITEM> = items.foldRight(empty(), ::Cons)
        fun <ITEM:Any> empty():List<ITEM> = Nil
    }
//    val length:Int = when(this){
//        is Nil -> 0
//        is Cons ->when(_tail){
//            is Nil -> 1
//            is Cons ->_tail.length + 1
//        }
//    }
    inline val head:ITEM? get() = when(this){
        is Nil -> null
        is Cons -> _head
    }
    inline val tail:List<ITEM> get() = when(this){
        is Nil -> Nil
        is Cons -> if(_tail is Nil) Nil else _tail
    }
}
inline fun <ITEM:Any> List<ITEM>.setHead(item: ITEM):List<ITEM> = when(this){
    is List.Nil -> List.Nil
    is List.Cons -> List.Cons(item, tail)
}
inline fun <ITEM:Any> List<ITEM>.addFirst(item: ITEM):List<ITEM> = when(this){
    is List.Nil -> List.Nil
    is List.Cons -> List.Cons(item, this)
}
//tailrec fun <ITEM:Any> List<ITEM>.drop(n:Int):List<ITEM> = when(n){
//    0 -> this
//    else -> when(this){
//        is List.Nil->List.Nil
//        is List.Cons->_tail.drop(n - 1)
//    }
//}
tailrec fun <ITEM:Any> List<ITEM>.drop(n:Int):List<ITEM> =
    if(n > 0 && this is List.Cons) _tail.drop(n - 1) else this
//tailrec fun <ITEM:Any> List<ITEM>.dropWhile(block:(ITEM)->Boolean):List<ITEM> = when(this){
//    is List.Nil -> List.Nil
//    is List.Cons -> if(block(_head)) _tail.dropWhile(block) else this
//}
tailrec fun <ITEM:Any> List<ITEM>.dropWhile(block:(ITEM)->Boolean):List<ITEM> =
    if(this is List.Cons && block(_head)) _tail.dropWhile(block) else this

//@PublishedApi internal tailrec fun <ITEM:Any> List<ITEM>._dropWhileIndexed(index:Int, block:(Int, ITEM)->Boolean):List<ITEM> = when(this){
//    is List.Nil -> List.Nil
//    is List.Cons -> if(block(index, _head)) _tail._dropWhileIndexed(index + 1, block) else this
//}
@PublishedApi internal tailrec fun <ITEM:Any> List<ITEM>._dropWhileIndexed(index:Int, block:(Int, ITEM)->Boolean):List<ITEM>
    = if(this is List.Cons && block(index, _head)) _tail._dropWhileIndexed(index + 1, block) else this
fun <ITEM:Any> List<ITEM>.dropWhileIndexed(block:(Int, ITEM)->Boolean):List<ITEM> = _dropWhileIndexed(0, block)
inline fun <ITEM:Any> List<ITEM>.drop2(n:Int):List<ITEM> = _dropWhileIndexed(0){index, _->index < n}
val <ITEM:Any> List<ITEM>.clone:List<ITEM> get() = append()
fun <ITEM:Any> List<ITEM>.append(list:List<ITEM> = List.empty()):List<ITEM> = when(this){
    is List.Nil -> list
    is List.Cons -> List.Cons(_head, _tail.append(list))
}
fun <ITEM:Any> List<ITEM>.dropLast():List<ITEM> = when(this){
    is List.Nil -> this
    is List.Cons -> if(_tail is List.Nil) List.Nil else List.Cons(_head, _tail.dropLast())
}
tailrec fun <ITEM:Any> List<ITEM>.dropLast(n:Int):List<ITEM>
    = if(n == 0) this else dropLast().dropLast(n - 1)
tailrec fun <ITEM:Any> List<ITEM>.dropLastWhile(block:(ITEM)->Boolean):List<ITEM>
    = if(this is List.Cons && block(_head)) dropLast().dropLastWhile(block) else this
@PublishedApi internal tailrec fun <ITEM:Any> List<ITEM>._dropLastWhileIndexed(index:Int, block:(Int, ITEM)->Boolean):List<ITEM>
    = if(this is List.Cons && block(index, _head)) dropLast()._dropLastWhileIndexed(index + 1, block) else this
fun <ITEM:Any> List<ITEM>.dropLastWhileIndexed(block:(Int, ITEM)->Boolean):List<ITEM> = _dropLastWhileIndexed(0, block)
tailrec fun <ITEM:Any, ACC:Any> List<ITEM>.fold(base:ACC, block:(ACC, ITEM)->ACC):ACC = when(this){
    is List.Nil -> base
    is List.Cons -> _tail.fold(block(base, _head), block)
}
@PublishedApi internal tailrec fun <ITEM:Any, ACC:Any> List<ITEM>._foldIndexed(index:Int, base:ACC, block:(Int, ACC, ITEM)->ACC):ACC = when(this){
    is List.Nil -> base
    is List.Cons -> _tail._foldIndexed(index + 1, block(index, base, _head), block)
}
fun <ITEM:Any, ACC:Any> List<ITEM>.foldIndexed(base:ACC, block:(Int, ACC, ITEM)->ACC):ACC = _foldIndexed(0, base, block)
fun <ITEM:Any, ACC:Any> List<ITEM>.foldRight(base:ACC, block:(ITEM, ACC)->ACC):ACC = when(this){
    is List.Nil -> base
    is List.Cons -> block(_head, _tail.foldRight(base, block))
}
@PublishedApi internal fun <ITEM:Any, ACC:Any> List<ITEM>._foldRightIndexed(index:Int, base:ACC, block:(Int, ITEM, ACC)->ACC):ACC = when(this){
    is List.Nil -> base
    is List.Cons -> block(index, _head, _tail._foldRightIndexed(index - 1, base, block))
}
fun <ITEM:Any, ACC:Any> List<ITEM>.foldRightIndexed(base:ACC, block:(Int, ITEM, ACC)->ACC):ACC = _foldRightIndexed(size - 1, base, block)
val <ITEM:Any> List<ITEM>.clone2:List<ITEM> get() = append2()
fun <ITEM:Any> List<ITEM>.append2(list:List<ITEM> = List.empty()):List<ITEM> = foldRight(list){it, acc->List.Cons(it, acc)}
fun <ITEM:Any> List<ITEM>.append3(list:List<ITEM> = List.empty()):List<ITEM> = reverse().fold(list){acc, it->List.Cons(it, acc)}

inline val <ITEM:Any> List<ITEM>.size:Int get() = fold(0){acc, _->acc + 1}
fun <ITEM:Any> List<ITEM>.dropLast2(n:Int = 1):List<ITEM> = foldRightIndexed(List.empty()){ index, it, acc->
    if(index >= n) List.Cons(it, acc) else List.empty()
}
inline fun List<Int>.sum():Int = fold(0){acc, it-> acc + it}
inline fun List<Long>.sum():Long = fold(0L){acc, it-> acc + it}
inline fun List<Float>.sum():Float = fold(0.0f){acc, it-> acc + it}
inline fun List<Double>.sum():Double = fold(0.0){acc, it-> acc + it}

inline fun List<Int>.product():Int = fold(0){acc, it-> acc * it}
inline fun List<Long>.product():Long = fold(0L){acc, it-> acc * it}
inline fun List<Float>.product():Float = fold(0.0f){acc, it-> acc * it}
inline fun List<Double>.product():Double = fold(0.0){acc, it-> acc * it}

fun <ITEM:Any> List<ITEM>.reverse():List<ITEM> = fold(List.empty()){acc, it->
    List.Cons(it, acc)
}
fun <ITEM:Any, ACC:Any> List<ITEM>.foldRight2(base:ACC, block:(ITEM, ACC)->ACC):ACC = reverse().fold(base){ acc, it->
    block(it, acc)
}


@PublishedApi internal tailrec fun <ITEM:Any, ACC:Any> List<ITEM>._foldRightIndexed2(index:Int, base:ACC, block:(Int, ITEM, ACC)->ACC):ACC = when(this){
    is List.Nil -> base
    is List.Cons -> _tail._foldRightIndexed2(index + 1, block(index, _head, base), block)
}
fun <ITEM:Any, ACC:Any> List<ITEM>.foldRightIndexed2(base:ACC, block:(Int, ITEM, ACC)->ACC):ACC = reverse()._foldRightIndexed2(0, base, block)

fun <ITEM:Any> List<List<ITEM>>.flatten():List<ITEM> = when(this){
    is List.Nil -> this
    is List.Cons -> drop(1).fold(_head){acc, it->
        acc.append(it)
    }
}