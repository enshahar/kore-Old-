@file:Suppress("NOTHING_TO_INLINE")

import org.junit.Test
import kotlin.test.assertEquals

class ListTest{
    @Test
    fun test1(){
        val list = List.of(1,2,3)
        assertEquals(list.head, 1)
        assertEquals(list.tail.head, 2)
        assertEquals(list.tail.tail.head, 3)
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
tailrec fun <ITEM:Any> _listDrop(acc:List<ITEM>, n:Int):List<ITEM> = when(n){
    0 -> acc
    else -> when(acc){
        is List.Nil->List.Nil
        is List.Cons->_listDrop(acc.tail, n - 1)
    }
}
inline fun <ITEM:Any> List<ITEM>.drop(n:Int):List<ITEM> = _listDrop(this, n)