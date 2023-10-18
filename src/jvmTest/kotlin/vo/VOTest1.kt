package vo

import kore.data.VO
import kore.data.field.value.int
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class VOTest1 {
    class Test1:VO(){
        var a1 by int{
            default(3)
        }
        var a2 by int
        var a3 by int(10)
    }
    @Test
    fun test1(){
        val vo1 = Test1().also {
            it.a1 = 5
            it.a2 = 7
        }
        val vo2 = Test1()
        assertEquals(vo1.a1, 5)
        assertEquals(vo1.a2, 7)
        assertEquals(vo1.a3, 10)
        assertEquals(vo2.a1, 3)
        assertFails { vo2.a2 }
        assertEquals(vo2.a3, 10)
    }
}