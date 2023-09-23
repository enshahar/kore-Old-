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
}
fun <ITEM:Any> Stream<ITEM>.toList():List<ITEM> = when(this){
    is Stream.Empty -> List()
    is Stream.Cons->List.Cons(_head(), _tail().toList())
}
fun <ITEM:Any> Stream<ITEM>.take(n:Int):Stream<ITEM>
 = if(this is Stream.Cons && n > 0) Stream(_head) { _tail().take(n - 1) } else Stream()
//= when(this){
//    is Stream.Empty -> Stream()
//    is Stream.Cons->if(n > 0) Stream(_head) { _tail().take(n - 1) } else Stream()
//}
tailrec fun <ITEM:Any> Stream<ITEM>.drop(n:Int):Stream<ITEM>
= when(this){
    is Stream.Empty -> Stream()
    is Stream.Cons->if(n > 0) _tail().drop(n - 1) else this
}
//tailrec operator fun <ITEM:Any> Stream<ITEM>.contains(block:(ITEM)->Boolean):Boolean = when(this){
//    is Stream.Empty -> false
//    is Stream.Cons->if(block(_head())) true else _tail().contains(block)
//}
fun <ITEM:Any,OTHER:Any> Stream<ITEM>.foldRight(emptyBlock:()->OTHER, consBlock:(ITEM, ()->OTHER)->OTHER):OTHER = when(this){
    is Stream.Empty -> emptyBlock()
    is Stream.Cons->consBlock(_head()){_tail().foldRight(emptyBlock, consBlock)}
}
fun <ITEM:Any> Stream<ITEM>.any(block:(ITEM)->Boolean):Boolean
    = foldRight({false}){it, acc->block(it) || acc()}
fun <ITEM:Any> Stream<ITEM>.all(block:(ITEM)->Boolean):Boolean
    = foldRight({true}){it, acc->block(it) && acc()}
fun <ITEM:Any> Stream<ITEM>.takeWhile(block:(ITEM)->Boolean):Stream<ITEM>
    = foldRight({Stream()}){it, acc->if(block(it)) Stream({it}, acc) else Stream()}
//= if(this is Stream.Cons && block(_head())) Stream(_head) { _tail().takeWhile(block) } else Stream()
//= when(this){
//    is Stream.Empty -> Stream()
//    is Stream.Cons->if(block(_head())) Stream(_head) { _tail().takeWhile(block) } else Stream()
//}

val <ITEM:Any> Stream<ITEM>.headOption:Option<ITEM> get()
    = foldRight({Option()}){it, _->Option(it)}
//= when(this){
//    is Stream.Empty->Option()
//    is Stream.Cons->Option(_head())
//}
fun <ITEM:Any, OTHER:Any> Stream<ITEM>.map(block:(ITEM)->OTHER):Stream<OTHER>
    = foldRight({Stream()}){it, acc->Stream({block(it)}, acc)}
fun <ITEM:Any> Stream<ITEM>.filter(block:(ITEM)->Boolean):Stream<ITEM>
    = foldRight({Stream()}){it, acc->if(block(it)) Stream({it}, acc) else acc()}
fun <ITEM:Any> Stream<ITEM>.append(other:()->Stream<ITEM>):Stream<ITEM>
    = foldRight(other){it, acc->Stream({it}, acc)}
