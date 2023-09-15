import kotlin.test.Test
import kotlin.test.assertEquals
class OptionTest {
    @Test
    fun test1(){
        val option = Option(3)
        assertEquals(option.map{it *2}.getOrElse { 0 }, 6)
        assertEquals(option.mapF{it *2}.getOrElse { 0 }, 6)
        assertEquals(option.flatMap { Option(it * 2) }.getOrElse { 0 }, 6)
        assertEquals(option.flatMapF { Option(it * 2) }.getOrElse { 0 }, 6)
        assertEquals(option.filter { it > 1}.getOrElse { 0 }, 3)
        assertEquals(option.filterF { it < 1 }.getOrElse { 0 }, 0)
        assertEquals(listOf(1,2,3).variance()

    }
}