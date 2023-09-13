import org.junit.Test
import kotlin.test.assertEquals

class ListTest{
    @Test
    fun test1(){
        val list = List.of(1, 2, 3)
        println("list $list")
        assertEquals(list.head, 1)
        assertEquals(list.tail.head, 2)
        assertEquals(list.tail.tail.head, 3)
        assertEquals(list.setHead(5).head, 5)
        assertEquals(list.addFirst(4).head, 4)
        assertEquals(list.addFirst(4).tail.head, 1)
        assertEquals(list.drop(2).head, 3)
        assertEquals(list.drop2(2).head, 3)
        assertEquals(list.dropWhile { it < 2 }.head, 2)
        assertEquals(list.dropWhileIndexed { index, it -> index < 1 }.head, 2)
        assertEquals(list.append().head, 1)
        assertEquals(list.append().tail.head, 2)
        assertEquals(list.append().tail.tail.head, 3)
        assertEquals(List.of(1).append(List.of(2, 3)).head, 1)
        assertEquals(List.of(1).append(List.of(2, 3)).tail.head, 2)
        assertEquals(List.of(1).append(List.of(2, 3)).tail.tail.head, 3)
        assertEquals(list.dropLast().tail.tail, List.Nil)
        assertEquals(list.dropLast(2).tail, List.Nil)
        assertEquals(list.dropLastWhile { it > 2 }.tail.head, 2)
        assertEquals(list.dropLastWhileIndexed { index, it -> index > 1 }.tail.head, 2)
        assertEquals(list.fold(3){acc, it->acc + it}, 9)
        assertEquals(list.foldRight(3){acc, it->acc + it}, 9)
        assertEquals(list.append2().head, 1)
        assertEquals(list.append2().tail.head, 2)
        assertEquals(list.append2().tail.tail.head, 3)
        assertEquals(list.dropLast2(2).tail, List.Nil)
        println("list.dropLast2(2) ${list.dropLast2(1)}")
        assertEquals(list.dropLast2(1).tail.head, 2)
        assertEquals(list.reverse().head, 3)
        assertEquals(list.reverse().tail.head, 2)
        assertEquals(list.reverse().tail.tail.head, 1)
        assertEquals(list.fold(""){acc, it->acc + it}, "123")
        assertEquals(list.foldIndexed(""){index, acc, it->"$acc$index$it"}, "011223")
        assertEquals(list.foldRight(""){it, acc->acc + it}, "321")
        assertEquals(list.foldRightIndexed(""){index, it, acc->"$acc$index$it"}, "031221")
        assertEquals(list.foldRight2(""){it, acc->acc + it}, "321")
        assertEquals(list.foldRightIndexed2(""){index, it, acc->"$acc$index$it"}, "031221")
    }

}