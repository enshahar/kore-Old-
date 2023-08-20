import kore.r.R
import kotlin.test.Test
import kotlin.test.assertEquals

class TestR {
    @Test
    fun test1(){
        val r1 = R.ok(5)
        assertEquals(r1(), 5)
        val r2 = R.ok { 3 }
        assertEquals(r2(), 3)
        val r3 = R.fail<Int>(Throwable("test"))
        assertEquals(r3(), null)
        assertEquals(r3{4}, 4)
        val r4 = R.failInt(Throwable("test"))
        assertEquals(r4(), null)
    }
    @Test
    fun test2(){
        val r1 = R.ok(5)
        val r2 = r1.map{it*3}.map{it+3}
        assertEquals(r2(), 18)
        assertEquals(r1.map{"a"}(), "a")
        val r3 = R.ok{4}
        val r4 = r3.map {
            println("map1")
            it * 2
        }.map{
            println("map2")
            it + 7
        }
        println("before lazy")
        assertEquals(r4(), 15)
        println("after lazy")
        assertEquals(r3.map{"a"}(), "a")
        val r5 = r3.map<Int>{
            println("m--")
            throw Throwable("1")
        }
        assertEquals(r5{ 10 }, 10)
        println("a")
        assertEquals(r5(), null)
        val r6 = R.ok(3)
        assertEquals(r6{7}, 3)
    }
}