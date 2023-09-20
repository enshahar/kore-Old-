import kotlin.collections.List
import kotlin.test.Test
import kotlin.test.assertEquals

class EitherTest {
    @Test
    fun test1(){
        val list = List(1.0, 2.0, 3.0)
        assertEquals(list.averageEither(), Either.Right(2.0))
        assertEquals(Either.right(3).orElse{Either.right(2)}, Either.right(3))
        assertEquals(Either.left(3).orElse{Either.right(2)}, Either.right(2))
        assertEquals(Either.left(3).map{5}, Either.left(3))
        assertEquals(Either.right(3).map{5}, Either.right(5))
        assertEquals(Either.left(3).flatMap{Either.right(5)}, Either.left(3))
        assertEquals(Either.right(3).flatMap{Either.right(5)}, Either.right(5))
        assertEquals(List(1,2,3).traverseEither{Either.right(it)}, Either.right(List(1,2,3)))
        assertEquals(List(Either.right(1),Either.right(2),Either.right(3)).sequenceEither(), Either.right(List(1,2,3)))
        assertEquals(List(Either.right(1),Either.left(2),Either.right(3)).sequenceEither(), Either.left(2))
        assertEquals(Either.left(1).map2Log<Int, Int, Int, Int>(Either.left(2)){a,b->3}, Either.left(listOf(1,2)))
    }
}