import kotlin.test.Test
import kotlin.test.assertEquals

class StreamTest {
    @Test
    fun test1(){
        assertEquals(Stream({1}, {Stream()}).toList().toString(), "Cons(_head=1, _tail=Nil)")
        assertEquals(Stream(1,2,3,4).toList().toString(), "Cons(_head=1, _tail=Cons(_head=2, _tail=Cons(_head=3, _tail=Cons(_head=4, _tail=Nil))))")
        assertEquals(Stream(1,2,3,4).take(2).toList().toString(), "Cons(_head=1, _tail=Cons(_head=2, _tail=Nil))")
        assertEquals(Stream(1,2,3,4).drop(2).toList().toString(), "Cons(_head=3, _tail=Cons(_head=4, _tail=Nil))")
        assertEquals(Stream(1,2,3,4).takeWhile{it<3}.toList().toString(), "Cons(_head=1, _tail=Cons(_head=2, _tail=Nil))")

    }
}