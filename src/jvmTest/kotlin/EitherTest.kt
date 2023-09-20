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
    }
}