import kore.r.R
import kotlin.test.Test
import kotlin.test.assertEquals

class TestR1 {
    @Test
    fun test1(){
        val r1 = R.ok(5)
        assertEquals(r1(), 5)
        val r2 = R.ok { 3 }
        assertEquals(r2(), 3)
        val r3 = R.fail<Int>(Throwable("test"))
        assertEquals(r3(), null)
        assertEquals(r3{4}, 4)
    }
}