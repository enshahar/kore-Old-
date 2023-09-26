package fd

import kore.fd.*
import kotlin.test.Test
import kotlin.test.assertEquals

class StreamTest {
    @Test
    fun test1(){
        assertEquals(FStream({1}, { FStream() }).toFList().toString(), "Cons(head=1, tail=Nil)")
        assertEquals(FStream(1,2,3,4).toFList().toString(), "Cons(head=1, tail=Cons(head=2, tail=Cons(head=3, tail=Cons(head=4, tail=Nil))))")
        assertEquals(FStream(1,2,3,4).take(2).toFList().toString(), "Cons(head=1, tail=Cons(head=2, tail=Nil))")
        assertEquals(FStream(1,2,3,4).drop(2).toFList().toString(), "Cons(head=3, tail=Cons(head=4, tail=Nil))")
        assertEquals(FStream(1,2,3,4).toList(), listOf(1,2,3,4))
        assertEquals(FStream(1,2,3,4).take(2).toList(), listOf(1,2))
        assertEquals(FStream(1,2,3,4).drop(2).toList(), listOf(3,4))
        assertEquals(FStream(1,2,3,4).takeWhile{it<3}.toList(), listOf(1,2))
        assertEquals(FStream(1,2,3,4).any{it == 2}, true)
        assertEquals(FStream(1,2,3,4).any{it == 5}, false)
        assertEquals(FStream(1,2,3,4).all{it < 5}, true)
        assertEquals(FStream(1,2,3,4).all{it < 4}, false)
        assertEquals(FStream(1,2,3,4).map{"-$it"}.toList(), listOf("-1", "-2", "-3", "-4"))
        assertEquals(FStream(1,2,3,4).filter{it<3}.toList(), listOf(1,2))
        assertEquals(FStream(1,2).append{ FStream(3,4) }.toList(), listOf(1,2,3,4))
        assertEquals((FStream(1,2) + FStream(3,4)).toList(), listOf(1,2,3,4))
        assertEquals(FStream(1,2).copy().toList(), listOf(1,2))
        assertEquals(FStream(1,2,3,4).flatMap{FStream("-$it")}.toList(), listOf("-1", "-2", "-3", "-4"))
        assertEquals(FStream(1,2,3,4).flatMap{if(it< 3) FStream("-$it") else FStream()}.toList(), listOf("-1", "-2"))
        assertEquals(FStream(1,2,3,4).flatMap{if(it % 2 == 0) FStream("-$it") else FStream()}.toList(), listOf("-2", "-4"))
        assertEquals({it == 1} in FStream(1,2), true)
        assertEquals({it == 5} in FStream(1,2), false)
        assertEquals(2 in FStream(1,2), true)
        assertEquals(7 in FStream(1,2), false)
    }
}