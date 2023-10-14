package fd

import kore.fd.FRand
import kore.fd.nextList
import kotlin.test.Test
import kotlin.test.assertEquals

class RandTest {
    @Test
    fun test1(){
        val rand1 = FRand.IntRand(42)
        val (r1, rand2) = rand1.nextInt()
        assertEquals(r1, 16159453)

        println(rand1.nextList(3))
    }
}


