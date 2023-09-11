@file:Suppress("NOTHING_TO_INLINE")

import org.junit.Test
import kotlin.test.assertEquals

class ListTest{
    @Test
    fun test1(){
        val list = List.of(1,2,3)
        println("list $list")
        assertEquals(list.head, 1)
        assertEquals(list.tail.head, 2)
        assertEquals(list.tail.tail.head, 3)
        assertEquals(list.setHead(5).head, 5)
        assertEquals(list.addFirst(4).head, 4)
        assertEquals(list.addFirst(4).tail.head, 1)
        assertEquals(list.drop(2).head, 3)
        assertEquals(list.drop2(2).head, 3)
        assertEquals(list.append().head, 1)
        assertEquals(list.append().tail.head, 2)
        assertEquals(list.append().tail.tail.head, 3)
        assertEquals(List.of(1).append(List.of(2,3)).head, 1)
        assertEquals(List.of(1).append(List.of(2,3)).tail.head, 2)
        assertEquals(List.of(1).append(List.of(2,3)).tail.tail.head, 3)
        println("list.dropLast() ${list.dropLast()}")
        println("list.dropLast().dropLast() ${list.dropLast().dropLast()}")
        println("list.dropLast(1) ${list.dropLast(1)}")
        println("list.dropLast(2) ${list.dropLast(2)}")
        assertEquals(list.dropLast().tail.tail, List.Nil)
        assertEquals(list.dropLast(2).tail, List.Nil)
    }
}

sealed class List<out ITEM:Any>{
    data object Nil:List<Nothing>()
    data class Cons<out ITEM:Any>(@PublishedApi internal val _head:ITEM, @PublishedApi internal val _tail:List<ITEM>):List<ITEM>()
    companion object{
        fun <ITEM:Any> of(vararg items:ITEM):List<ITEM>{
            return if(items.isEmpty()) Nil else _of(Cons(items.last(), Nil), items.dropLast(1))
        }
        private tailrec fun <ITEM:Any> _of(acc: List<ITEM>, items: kotlin.collections.List<ITEM>): List<ITEM> {
            return if(items.isEmpty()) acc else _of(Cons(items.last(), acc), items.dropLast(1))
        }
    }
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
fun <ITEM:Any> List<ITEM>.append(list:List<ITEM>? = null):List<ITEM> = when(this){
    is List.Nil -> list ?: List.Nil
    is List.Cons -> List.Cons(_head, _tail.append(list))
}
fun <ITEM:Any> List<ITEM>.dropLast():List<ITEM> = when(this){
    is List.Nil -> List.Nil
    is List.Cons -> if(_tail is List.Nil) List.Nil else List.Cons(_head, _tail.dropLast())
}
@PublishedApi internal tailrec fun <ITEM:Any> _dropLast(acc:List<ITEM>, n:Int):List<ITEM>
    = if(n == 0) acc else _dropLast(acc.dropLast(), n - 1)
fun <ITEM:Any> List<ITEM>.dropLast(n:Int):List<ITEM> = _dropLast(this, n)