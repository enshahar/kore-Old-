package fd

import kore.fd.*
import kotlin.test.Test
import kotlin.test.assertEquals
class TreeTest {
    @Test
    fun test1(){
        val tree = Tree(Tree(Tree(1), Tree(2)), Tree(Tree(7), Tree(4)))
//        assertEquals(tree.size(), 4)
//        assertEquals(tree.size2(), 4)
//        assertEquals(tree.size3(), 4)
//        assertEquals(tree.fold(Int.MIN_VALUE){acc, it-> if(acc < it) it else acc}, 7)
//        assertEquals(tree.max(), 7)
//        assertEquals(Tree.of(Tree.of(1,2), Tree.of(3)).depth(), 3)

        val tree2 = Tree(Tree(Tree(1), Tree(Tree(3), Tree(4))), Tree(5))
        assertEquals(tree2.depth, 4)
        assertEquals(tree2.depthF, 4)
        assertEquals(tree2.size, 7)
        assertEquals(tree2.sizeF, 7)
        assertEquals(tree2.max, 5)
        assertEquals(tree2.maxF, 5)
        assertEquals("${tree2.map{it*2}}", "Branch(left=Branch(left=Leaf(item=2), right=Branch(left=Leaf(item=6), right=Leaf(item=8))), right=Leaf(item=10))")
        assertEquals("${tree2.mapF{it*2}}", "Branch(left=Branch(left=Leaf(item=2), right=Branch(left=Leaf(item=6), right=Leaf(item=8))), right=Leaf(item=10))")
        assertEquals(1 in tree2, true)
        assertEquals(3 in tree2, true)
        assertEquals(4 in tree2, true)
        assertEquals(5 in tree2, true)
        assertEquals(2 in tree2, false)
        assertEquals( 5 in tree2, true)
    }
}