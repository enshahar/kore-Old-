package vo

import kore.data.VO
import kotlin.test.Test
import kotlin.test.assertEquals

class VOTest1 {
    class Test1:VO(){
        var a by int()
    }
    @Test
    fun test1(){
        val vo1 = Test1().also { it.a = 5 }
        assertEquals(vo1.a, 5)
        println(VO.fields(Test1::class))
    }
}