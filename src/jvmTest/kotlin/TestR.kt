import kore.wrap.W
import kotlin.test.Test
import kotlin.test.assertEquals

class TestR {
    @Test
    fun test1(){
        val r1 = W(5)
        assertEquals(r1(), 5)
        val r2 = W { 3 }
        assertEquals(r2(), 3)
        val r3 = W<Int>(Throwable("test"))
        assertEquals(r3(), null)
        assertEquals(r3{4}, 4)
    }
    @Test
    fun test2(){
        var count = 0
        val r1 = W(5)
        val r2 = r1.map{it*3}.map{it+3}
        assertEquals(r2(), 18)
        assertEquals(r1.map{"a"}(), "a")
        val r3 = W{4}
        val r4 = r3.map {
            count++
            it * 2
        }.map{
            count++
            it + 7
        }
        assertEquals(count, 0)
        assertEquals(r4(), 15)
        assertEquals(count, 2)
        assertEquals(r3.map{"a"}(), "a")
        val r5 = r3.map<Int>{
            throw Throwable("1")
        }
        Result
        assertEquals(r5{ 10 }, 10)
        assertEquals(r5(), null)
        val r6 = W(3)
        assertEquals(r6{7}, 3)
    }
    @Test
    fun test3(){
        var lazyCount = 0
        val r1 = W(3)
        val r2 = r1.mapLazy {
            lazyCount++
            5
        }.map {
            lazyCount++
            "a"
        }
        assertEquals(lazyCount, 0)
        assertEquals(r2(), "a")
        assertEquals(lazyCount, 2)
    }
    @Test
    fun test4(){
        assertEquals(W.catch {
            3
        }(), 3)
        assertEquals(W.catch<Int>{
            throw Throwable("aa")
        }(), null)
    }
}