package fd

import kore.wrap.*
import org.junit.Test
import kotlin.test.assertEquals

class WListTest{
    @Test
    fun test1(){
        val list = WList(1, 2, 3)
        val nil = WList<Int>()
        assertEquals(list.size, 3)
        assertEquals(nil.size, 0)
        assertEquals(list.toList(), listOf(1, 2, 3))
        assertEquals(list.setHead(5).toList(), listOf(5, 2, 3))
        assertEquals(nil.setHead(5).toString(), "Nil")
        assertEquals(list.setHeadW(W(5)).toList(), listOf(5, 2, 3))
        assertEquals(nil.setHeadW(W(5)).toString(), "Nil")
        assertEquals(list.setHeadW(W(Throwable(""))).toList(), listOf())
        assertEquals(nil.setHeadW(W(Throwable(""))).toString(), "Nil")
        assertEquals(list.fold(""){acc,it->acc + it}, "123")
        assertEquals(list.foldW(W("")){ acc, item->acc.map{"$it$item"} }(), "123")
        assertEquals(list.foldW(W("")){ acc, it->W.end() }.ok, null)
        assertEquals(list.addFirst(4).toList(), listOf(4,1,2,3))
        assertEquals(nil.addFirst(4).toString(), "Nil")
        assertEquals(list.foldIndexed(""){index, acc,it->"$acc$index$it"}, "011223")
        assertEquals(list.reverse().toList(), listOf(3,2,1))
        assertEquals(list.reverseW()()?.toList(), listOf(3,2,1))
        assertEquals(list.foldRight(""){it, acc->acc + it}, "321")
        assertEquals(list.foldRightW(W("")){item, acc->acc.map{"$it$item"}}(), "321")
        assertEquals(list.foldRightIndexed(""){index, it, acc->"$acc$index$it"}, "031221")
        assertEquals(list.map{"${it*2}"}.toList(), listOf("2","4","6"))
        assertEquals(list.flatMap{ if(it != 2) WList(it) else WList() }.toList(), listOf(1,3))
//        assertEquals(FList(FList(1,2), FList(3,4)).flatten().toList(), listOf(1,2,3,4))
//        assertEquals(list.append().toList(), listOf(1,2,3))
//        assertEquals(list.append(FList(4, 5)).toList(), listOf(1,2,3,4,5))
//        assertEquals((list + FList.Nil).toList(), listOf(1,2,3))
//        assertEquals((list + FList(4,5)).toList(), listOf(1,2,3,4,5))
//        assertEquals(list.copy().toList(), listOf(1,2,3))
//        assertEquals(list.drop(2).toString(), "Cons(head=3, tail=Nil)")
//        assertEquals(list.dropWhile { it < 2 }.toString(), "Cons(head=2, tail=Cons(head=3, tail=Nil))")
//        assertEquals(list.dropWhileIndexed { index, it -> index < 1 }.toString(), "Cons(head=2, tail=Cons(head=3, tail=Nil))")
//        assertEquals(list.dropLast().toList(), listOf(1,2))
//        assertEquals(list.dropLast(2).toList(), listOf(1))
//        assertEquals(list.dropLastWhile { it > 2 }.toList(), listOf(1,2))
//        assertEquals(nil.dropLast().toList(), listOf())
//        assertEquals(nil.dropLast(2).toList(), listOf())
//        assertEquals(nil.dropLastWhile { it > 2 }.toList(), listOf())
//        assertEquals(list.dropLastWhileIndexed { index, it ->index < 1}.toList(), listOf(1, 2))
//        assertEquals(list.filter{it != 2}.toList(), listOf(1,3))
//        assertEquals(FList(1, 2, 3, 4).filter{it % 2 == 0}.toList(), listOf(2,4))
//        assertEquals(FList(1, 2, 3, 4).sliceFrom(3).toList(), listOf(3,4))
//        assertEquals(FList(1, 2, 3, 4).slice(1).toList(), listOf(2,3,4))
//        assertEquals(FList(1, 2, 3, 4).slice(1,2).toList(), listOf(2,3))
//        assertEquals(nil.sliceFrom(3).toList(), listOf())
//        assertEquals(nil.slice(1).toList(), listOf())
//        assertEquals(nil.slice(1,2).toList(), listOf())
//        assertEquals(list.startsWith(FList(2,3)), false)
//        assertEquals(list.startsWith(FList(1,2,3)), true)
//        assertEquals(list.startsWith(FList(1,3)), false)
//        assertEquals(FList(1,2,3).startsWith(FList(1,2)), true)
//        assertEquals(list.startsWith(FList()), false)
//        assertEquals(nil.startsWith(FList()), true)
//        assertEquals(nil.startsWith(FList(1,2)), false)
//        assertEquals(FList(1,2) in list, true)
//        assertEquals(FList(2,3) in list, true)
//        assertEquals(FList(1,2,3) in list, true)
//        assertEquals(FList(1,3) in list, false)
//        assertEquals(FList() in list, false)
//        assertEquals(FList(1,3) in nil, false)
//        assertEquals(FList() in nil, true)
//        assertEquals(1 in list, true)
//        assertEquals(2 in list, true)
//        assertEquals(3 in list, true)
//        assertEquals(4 in list, false)
//        assertEquals(1 in nil, false)
//        assertEquals(FList.invoke(1, 2, 3).zipWith(FList.invoke(1,1,1)){ a, b->a + b}.toList(), listOf(2,3,4))
//        assertEquals(FList.invoke(1, 2).zipWith(FList.invoke(1,1,1)){ a, b->a + b}.toList(), listOf(2,3))
//        assertEquals(FList.invoke(1, 2, 3).zipWith(FList.invoke(1,1)){ a, b->a + b}.toList(), listOf(2,3))
    }

}