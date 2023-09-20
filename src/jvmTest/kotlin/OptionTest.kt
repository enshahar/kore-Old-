import kotlin.test.Test
import kotlin.test.assertEquals
class OptionTest {
    @Test
    fun test1(){
        val option = Option(3)
        assertEquals(option.map{it *2}.getOrElse { 0 }, 6)
        assertEquals(option.flatMap { Option(it * 2) }.getOrElse { 0 }, 6)
        assertEquals(option.filter { it > 1}.getOrElse { 0 }, 3)
        assertEquals(option.filterF { it < 1 }.getOrElse { 0 }, 0)
        assertEquals(listOf(1.0,2.0,3.0,4.0).variance().getOrElse { 0.0 }, 1.25)
        assertEquals(option.map2(Option(2)){a, b->a + b}.getOrElse { 0 }, 5)
        assertEquals(option.map2F(Option(2)){a, b->a + b}.getOrElse { 0 }, 5)
        assertEquals(List(Option(1),Option(3)).sequence().toString(), "Some(value=Cons(_head=1, _tail=Cons(_head=3, _tail=Nil)))")
        assertEquals(List(Option(1), Option(), Option(3)).sequence().toString(), "None")
        println("-------")
        assertEquals(List(Option(1), Option(), Option(3)).sequenceT().toString(), "None")
        assertEquals(List(Option(1),Option(3)).sequenceT().toString(), "Some(value=Cons(_head=1, _tail=Cons(_head=3, _tail=Nil)))")
    }
}