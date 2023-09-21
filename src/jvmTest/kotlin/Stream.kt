sealed class Stream<out ITEM:Any> {
    companion object{
        operator fun <ITEM:Any> invoke(head:()->ITEM, tail:()->Stream<ITEM>):Stream<ITEM>{
            val h by lazy(head)
            val t by lazy(tail)
            return Cons({h}, {t})
        }
        operator fun <ITEM:Any> invoke():Stream<ITEM> = Empty
        operator fun <ITEM:Any> invoke(vararg items:ITEM):Stream<ITEM> = items.foldRight(invoke()){ it, acc ->
            Stream({it}, {acc})
        }
    }
    data object Empty:Stream<Nothing>()
    data class Cons<out ITEM:Any>(val _head:()->ITEM, val _tail:()->Stream<ITEM>):Stream<ITEM>()
    val headOption:Option<ITEM> get() = when(this){
        is Empty->Option()
        is Cons->Option(_head())

    }
}
fun <ITEM:Any> Stream<ITEM>.toList():List<ITEM> = when(this){
    is Stream.Empty -> List()
    is Stream.Cons->List.Cons(_head(), _tail().toList())
}
fun <ITEM:Any> Stream<ITEM>.take(n:Int):Stream<ITEM> = when(this){
    is Stream.Empty -> Stream()
    is Stream.Cons->if(n > 0) Stream(_head) { _tail().take(n - 1) } else Stream()
}
fun <ITEM:Any> Stream<ITEM>.takeWhile(block:(ITEM)->Boolean):Stream<ITEM> = when(this){
    is Stream.Empty -> Stream()
    is Stream.Cons->if(block(_head())) Stream(_head) { _tail().takeWhile(block) } else Stream()
}
tailrec fun <ITEM:Any> Stream<ITEM>.drop(n:Int):Stream<ITEM>
 =if(this is Stream.Cons && n > 0) _tail().drop(n - 1) else this
//= when(this){
//    is Stream.Empty -> Stream()
//    is Stream.Cons->if(n > 0) _tail().drop(n - 1) else this
//}