package fd

import kore.fd.*
import kotlin.test.Test
import kotlin.test.assertEquals

class RandTest {
    @Test
    fun test1(){
        val rand1 = FRand.IntRand(42)
        val (r1, rand2) = rand1.nextInt()
        assertEquals(r1, 16159453)
        val (list, rand3) = rand1.nextIntList(3)
        assertEquals(list.toList().first(), 16159453)
        assertEquals(list.size, 3)
        assertEquals(rand1.nextNonNegative().first > 0, true)
    }
}


