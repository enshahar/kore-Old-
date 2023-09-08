import kore.data.Data
import kore.data.converter.encodeKore
import org.junit.Test
import kotlin.test.assertEquals

class TestData {
    class Test1:Data(){
        var name by string
        var age by int
    }
    @Test
    fun test1(){
        val test1 = Test1().also {
            it.name = "hika"
            it.age = 17
        }
        val encode1 = test1.encodeKore()
        assertEquals(encode1(), "hika|17|")
    }
}