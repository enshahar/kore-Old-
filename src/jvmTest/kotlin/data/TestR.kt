//package data
//
//import kore.wrap.W
//import kotlin.test.Test
//import kotlin.test.assertEquals
//
//class TestR {
//    @Test
//    fun test1(){
//        val r1 = W(5)
//        assertEquals(r1(), 5)
//        val r2 = W { 3 }
//        assertEquals(r2(), 3)
//        val r3 = W<Int>(Throwable("test"))
//        assertEquals(r3(), null)
//        assertEquals(r3{4}, 4)
//    }
//    @Test
//    fun test2(){
//        var count = 0
//        val r1 = W(5)
//        val r2 = r1.map{it*3}.map{it+3}
//        assertEquals(r2(), 18)
//        assertEquals(r1.map{"a"}(), "a")
//        val r3 = W{4}
//        val r4 = r3.map {
//            count++
//            it * 2
//        }.map{
//            count++
//            it + 7
//        }
//        assertEquals(count, 0)
//        assertEquals(r4(), 15)
//        assertEquals(count, 2)
//        assertEquals(r3.map{"a"}(), "a")
//        val r6 = W(3)
//        assertEquals(r6{7}, 3)
//    }
//    @Test
//    fun test3(){
//        var lazyCount = 0
//        val r1 = W(3)
//        val r2 = r1.mapLazy {
//            lazyCount++
//            5
//        }.map {
//            lazyCount++
//            "a"
//        }
//        assertEquals(lazyCount, 0)
//        assertEquals(r2(), "a")
//        assertEquals(lazyCount, 2)
//    }
//    @Test
//    fun test4(){
//        assertEquals(W.catch {
//            3
//        }(), 3)
//        assertEquals(W.catch<Int>{
//            throw Throwable("aa")
//        }(), null)
//    }
//    /** 복잡한 map */
//    @Test
//    fun test5(){
//        val wrap = W(arrayListOf(1,2,3,4,5))
//        val wrap1 = wrap.map { it.map{it*2} }
//        assertEquals(wrap1(), arrayListOf(2,4,6,8,10))
//        val wrap2 = wrap.flatMap {
//            it.flatMapList {
//                if(it % 2 == 0) W(it) else W(Throwable("test"))
//            }
//        }
//        assertEquals(wrap2(), null)
//        assertEquals(wrap2.isEffected()?.message, "test")
//        val wrap3 = wrap.flatMap {
//            it.flatMapList {
//                W(it*2)
//            }
//        }
//        assertEquals(wrap3(), arrayListOf(2,4,6,8,10))
//        val wrap4 = W(arrayListOf("a","1","b","2","c","3"))
//        val wrap5 = wrap4.flatMap {
//            it.flatMapListToMap { key, value ->
//                value.toIntOrNull()?.let{W(it)} ?: W(Throwable("not int $value"))
//            }
//        }
//        assertEquals(wrap5(), hashMapOf("a" to 1, "b" to 2, "c" to 3))
//        val wrap6 = W(arrayListOf("a","1","b","k","c","3")).flatMap {
//            it.flatMapListToMap { key, value ->
//                value.toIntOrNull()?.let{W(it)} ?: W(Throwable("not int $value"))
//            }
//        }
//        assertEquals(wrap6.isEffected()?.message, "not int k")
//    }
//    /**lazy실험 */
//    @Test
//    fun test6(){
//        val wrap1 = W(arrayListOf(1,2,3,4,5)).mapLazy {
//            it.map{it*2}
//        }
//        val wrap2 = wrap1.flatMap {
//            it.flatMapList {
//                W(it*3)
//            }
//        }
//        assertEquals(wrap2(), arrayListOf(6,12,18,24,30))
//        val wrap3 = wrap1.flatMap {
//            it.flatMapList {
//                if(it > 2) W(Throwable("big $it")) else W(it*3)
//            }
//        }
//        assertEquals(wrap3.isEffected()?.message, "big 4")
//        val wrap4 = W(arrayListOf(1,2,3,4,5)).flatMapLazy {
//            it.flatMapList {
//                W(it*3)
//            }
//        }
//        val wrap5 = wrap4.flatMap {
//            it.flatMapList {
//                W(it + 1)
//            }
//        }
//        assertEquals(wrap5(), arrayListOf(4,7,10,13,16))
//        val wrap6 = wrap5.flatMap {
//            it.flatMapList {
//                if(it > 10) W(Throwable("big $it")) else W(it + 1)
//            }
//        }
//        assertEquals(wrap6.isEffected()?.message, "big 13")
//    }
//}