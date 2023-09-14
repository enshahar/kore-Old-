import kotlin.test.Test
import kotlin.test.assertEquals
class TreeTest {
    @Test
    fun test1(){
        val tree = Tree.of(Tree.of(1,2), Tree.of(3, 4))
        assertEquals(tree.size(), 4)
        assertEquals(tree.size2(), 4)
        assertEquals(tree.fold(Int.MIN_VALUE){acc, it-> if(acc < it) it else acc}, 4)
        assertEquals(tree.max(), 4)
        assertEquals(Tree.of(Tree.of(1,2), Tree.of(3)).depth(), 3)
    }
}