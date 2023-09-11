import kotlin.test.Test
import kotlin.test.assertEquals

class a {
    fun test2(){
        assertEquals(fib(0), 0)
        assertEquals(fib(1), 1)
        assertEquals(fib(2), 1)
        assertEquals(fib(3), 2)
        assertEquals(fib(4), 3)
        assertEquals(fib(5), 5)
    }
    @Test
    fun testIsSorted(){
//        assertEquals(arrayListOf(1,2,3,4).isSorted(Int::less), true)
    }
    @Test
    fun testCurry(){
        val c1 = curry{a:Int, b:Int->Int
            a + b
        }
        assertEquals(c1(2)(5), 7)
        assertEquals(c1(1)(3), 4)
        val c11 = uncurry(c1)
        assertEquals(c11(2,5), 7)
        val block = {a:Int, b:Int->Int
            a + b
        }
        assertEquals(block.curry(2)(5), 7)
        assertEquals(block.curry(1)(3), 4)
    }
    @Test
    fun testCompose(){
        //val f = compose({it:Int->it*2}, {it:Int->it+5})
        val f = {it:Int->it+5} compose { it*2 }
        assertEquals(f(2), 14)
    }
}
//fun <P, RESULT, RETURN> compose(result:(RESULT)->RETURN, block:(P)->RESULT):(P)->RETURN = {p->result(block(p))}
infix fun <P, RESULT, RETURN> ((P)->RESULT).compose(block:(RESULT)->RETURN):(P)->RETURN = {p->block(this(p))}
fun <P1, P2, RETURN> uncurry(block:(P1)->(P2)->RETURN):(P1, P2)->RETURN = {p1, p2->block(p1)(p2)}

fun <P1, P2, RETURN> curry(block:(P1, P2)->RETURN):(P1)->(P2)->RETURN = {p1->{p2->block(p1, p2)}}
fun <P1, P2, RETURN> ((P1, P2)->RETURN).curry(p1:P1):(P2)->RETURN = {p2->this(p1, p2)}

//tailrec fun _ufib(curr:UInt, limit:UInt, prevprev:UInt, prev:UInt):UInt = if(curr == limit) prevprev + prev else _ufib(curr + 1u, limit, prev, prevprev + prev)
//fun ufib(i:UInt): UInt = when(i) {
//    in 0u..1u -> i
//    else -> _ufib(2u, i, 0u, 1u)
//}

tailrec fun _fib(curr:Int, limit:Int, prevprev:Int, prev:Int):Int = if(curr == limit) prevprev + prev else _fib(curr + 1, limit, prev, prevprev + prev)
fun fib(i:Int): Int = when(i) {
    in 0..1 -> i
    else -> _fib(2, i, 0, 1)
}
//
//inline val <ITEM:Any> List<ITEM>.tail: List<ITEM> get() = drop(1)
//inline val <ITEM:Any> List<ITEM>.head: ITEM get() = first()
//inline fun <NUM:Comparable<NUM>> NUM.above(b:NUM):Boolean = this > b
//inline fun <NUM:Comparable<NUM>> NUM.more(b:NUM):Boolean = this >= b
//inline fun <NUM:Comparable<NUM>> NUM.under(b:NUM):Boolean = this < b
//inline fun <NUM:Comparable<NUM>> NUM.less(b:NUM):Boolean = this <= b
//tailrec fun <ITEM:Any> _isSorted(head:ITEM, tail:List<ITEM>, check:ITEM.(ITEM)->Boolean):Boolean{
//    val tailHead = tail.head
//    val tailTail = tail.tail
//
//    return when{
//        !check(head, tailHead)-> false
//        tailTail.isEmpty()-> true
//        else->_isSorted(tailHead, tailTail, check)
//    }
//
//}
//fun <ITEM:Any> List<ITEM>.isSorted(check:ITEM.(ITEM)->Boolean):Boolean = when(size){
//    in 0..1->true
//    else->_isSorted(head, tail, check)
//}

