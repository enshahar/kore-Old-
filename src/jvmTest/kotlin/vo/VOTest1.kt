package vo

import kore.data.VO
import kore.data.field.enum.enum
import kore.data.field.enum.enumList
import kore.data.field.enum.enumMap
import kore.data.field.list.doubleList
import kore.data.field.map.stringMap
import kore.data.field.value.int
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class VOTest1 {
    enum class Enum1{
        A,B,C
    }
    class Test1:VO(){
        var a1 by int{
            default(3)
        }
        var a2 by int
        var a3 by int(10)
        var b1 by doubleList
        var b2 by doubleList(1.0,2.0,3.0)
        var b3 by doubleList(arrayListOf(1.0,2.0,3.0))

        var c1 by stringMap
        var c2 by stringMap("a" to "1","b" to "2")
        var c3 by stringMap(hashMapOf("a" to "1", "b" to "2"))

        var d1 by enum<Enum1>()
        var d2 by enumList<Enum1>(Enum1.A, Enum1.B, Enum1.C)
        var d3 by enumList<Enum1>(arrayListOf(Enum1.A, Enum1.B, Enum1.C) )
        var d4 by enumMap<Enum1>("a" to Enum1.A, "b" to Enum1.B)
        var d5 by enumMap<Enum1>(hashMapOf("a" to Enum1.A, "b" to Enum1.B))

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
        assertFails { vo1.b1 }
        assertEquals(vo1.b2, arrayListOf(1.0,2.0,3.0))
        assertEquals(vo1.b3, arrayListOf(1.0,2.0,3.0))
        assertFails { vo1.c1 }
        assertEquals(vo1.c2, hashMapOf("a" to "1", "b" to "2"))
        assertEquals(vo1.c3, hashMapOf("a" to "1", "b" to "2"))
        assertFails { vo1.d1 }
        assertEquals(vo1.d2, arrayListOf(Enum1.A, Enum1.B, Enum1.C))
        assertEquals(vo1.d3, arrayListOf(Enum1.A, Enum1.B, Enum1.C))
        assertEquals(vo1.d4, hashMapOf("a" to Enum1.A, "b" to Enum1.B))
        assertEquals(vo1.d5, hashMapOf("a" to Enum1.A, "b" to Enum1.B))
        assertEquals(vo2.a1, 3)
        assertFails { vo2.a2 }
        assertEquals(vo2.a3, 10)
    }
}