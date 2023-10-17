//package data
//
//import kore.data.VO
//import kore.data.converter.encodeKore
//import org.junit.Test
//import kotlin.test.assertEquals
//
//class TestData {
//    class Response1:VO(){
//        var name by string
//        var age by int
//    }
//    class Request1:VO(){
//        var a by string
//        var b by intList
//        var c by int
//        var d by intMap
//        var e by data(TestData::Response1)
//    }
//    @Test
//    fun test1(){
//        val test1 = Response1().also {
//            it.name = "hika"
//            it.age = 17
//        }
//        val encode1 = test1.encodeKore()
//        assertEquals(encode1(), "hika|17|")
//        val test2 = Request1().also {
//            it.a = "a"
//            it.b = arrayListOf(1,2,3)
//            it.c = 3
//            it.d = hashMapOf("a" to 1, "b" to 2)
//            it.e = Response1().also {
//                it.name = "hika"
//                it.age = 17
//            }
//        }
//        assertEquals(test2.encodeKore()(), "a|1|2|3@|3|a|1|b|2@|hika|17||")
//    }
//}
