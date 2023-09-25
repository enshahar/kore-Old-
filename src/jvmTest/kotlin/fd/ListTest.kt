package fd

import kore.fd.*
import kore.fd.FList
import org.junit.Test
import kotlin.test.assertEquals

class ListTest{
    @Test
    fun test1(){
        val list = FList(1, 2, 3)
//        assertEquals(list.head, 1)
//        assertEquals(list.tail.head, 2)
//        assertEquals(list.tail.tail.head, 3)
//        assertEquals(list.setHead(5).head, 5)
//        assertEquals(list.addFirst(4).head, 4)
//        assertEquals(list.addFirst(4).tail.head, 1)
//        assertEquals(list.drop(2).head, 3)
//        assertEquals(list.drop2(2).head, 3)
//        assertEquals(list.dropWhile { it < 2 }.head, 2)
//        assertEquals(list.dropWhileIndexed { index, it -> index < 1 }.head, 2)
//        assertEquals(list.append().head, 1)
//        assertEquals(list.append().tail.head, 2)
//        assertEquals(list.append().tail.tail.head, 3)
//        assertEquals(FList.invoke(1).append(FList.invoke(2, 3)).head, 1)
//        assertEquals(FList.invoke(1).append(FList.invoke(2, 3)).tail.head, 2)
//        assertEquals(FList.invoke(1).append(FList.invoke(2, 3)).tail.tail.head, 3)
//        assertEquals(list.dropLast().tail.tail, FList.Nil)
        assertEquals(list.dropLast(2).toString(), "Cons(head=1, tail=Nil)")
//        assertEquals(list.dropLastWhile { it > 2 }.tail.head, 2)
//        assertEquals(list.dropLastWhileIndexed { index, it -> index > 1 }.tail.head, 2)
//        assertEquals(list.fold(3){acc, it->acc + it}, 9)
//        assertEquals(list.foldRight(3){acc, it->acc + it}, 9)
//        assertEquals(list.append2().head, 1)
//        assertEquals(list.append2().tail.head, 2)
//        assertEquals(list.append2().tail.tail.head, 3)
//        assertEquals(list.append3().head, 1)
//        assertEquals(list.append3().tail.head, 2)
//        assertEquals(list.append3().tail.tail.head, 3)
//        assertEquals(list.dropLast2(2).tail, FList.Nil)
//        assertEquals(list.dropLast2(1).tail.head, 2)
//        assertEquals(list.reverse().head, 3)
//        assertEquals(list.reverse().tail.head, 2)
//        assertEquals(list.reverse().tail.tail.head, 1)
//        assertEquals(list.fold(""){acc, it->acc + it}, "123")
//        assertEquals(list.foldIndexed(""){index, acc, it->"$acc$index$it"}, "011223")
//        assertEquals(list.foldRight(""){it, acc->acc + it}, "321")
//        assertEquals(list.foldRightIndexed(""){index, it, acc->"$acc$index$it"}, "031221")
//        assertEquals(list.foldRight2(""){it, acc->acc + it}, "321")
//        assertEquals(list.foldRightIndexed2(""){index, it, acc->"$acc$index$it"}, "031221")
        val listlist = FList(FList(1,2), FList(3,4))
        assertEquals(listlist.flatten().toString(), "Cons(head=1, tail=Cons(head=2, tail=Cons(head=3, tail=Cons(head=4, tail=Nil))))")
//        assertEquals(list.map{"${it*2}"}.fold(""){acc, it->"$acc$it"}, "246")
//        assertEquals(list.filter{it != 2}.fold(""){acc, it->"$acc$it"}, "13")
//        assertEquals(FList.invoke(1, 2, 3, 4).filter{it % 2 == 0}.fold(""){ acc, it->"$acc$it"}, "24")
//        assertEquals(list.flatMap { kore.fd.FList(0, it) }.fold(""){ acc, it->"$acc$it"}, "010203")
//        assertEquals(list.flatMap2 { kore.fd.FList(0, it) }.fold(""){ acc, it->"$acc$it"}, "010203")
//        assertEquals(FList.invoke(1, 2, 3, 4).filter2{it % 2 == 0}.fold(""){ acc, it->"$acc$it"}, "24")
//        assertEquals(FList.invoke(1, 2, 3).zipWith(FList.invoke(1,1,1)){ a, b->a + b}.fold(""){ acc, it->"$acc$it"}, "234")
//        assertEquals(FList.invoke(1, 2, 3, 4).hasSubSequence(FList.invoke(2)), true)
//        assertEquals(FList.invoke(1, 2, 3, 4).hasSubSequence(FList.invoke(2, 3)), true)
//        assertEquals(FList.invoke(1, 2, 3, 4).hasSubSequence(FList.invoke(2, 3,4)), true)
//        assertEquals(FList.invoke(1, 2, 3, 4).hasSubSequence(FList.invoke(1,2, 3)), true)
//        assertEquals(FList.invoke(1, 2, 3, 4).hasSubSequence(FList.invoke(3, 4)), true)
//        assertEquals(FList.invoke(1, 2, 3, 4).hasSubSequence(FList.invoke(1, 2, 3, 4)), true)
//        assertEquals(FList.invoke(1, 2, 3, 4).hasSubSequence(FList.invoke(1, 3)), false)
    }

}