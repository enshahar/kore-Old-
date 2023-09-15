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
        assertEquals(listOf(1.0,2.0,3.0,4.0).variance().getOrElse { 0.0 }, 1.25)
        assertEquals(List.of(1.0,2.0,3.0,4.0).variance().getOrElseF { 0.0 }, 1.25)
        assertEquals(option.map2(Option(2)){a, b->a + b}.getOrElse { 0 }, 5)
        assertEquals(option.map2F(Option(2)){a, b->a + b}.getOrElse { 0 }, 5)
        assertEquals(List.of(Option(1),Option<Int>(),Option(3)).sequence().getOrElse { List.empty() }.toString(), "1")
    }
}