@file:Suppress("NOTHING_TO_INLINE")

package kore.fd

import kore.fd.FEither.Left
import kore.fd.FEither.Right
import kore.fd.FList.Cons
import kore.fd.FList.Nil

sealed class FEither<out LEFT, out RIGHT> {
    companion object {
        fun <RIGHT : Any> catches(block: () -> RIGHT): FEither<Throwable, RIGHT> = try {
            Right(block())
        } catch (e: Throwable) {
            Left(e)
        }
        fun <RIGHT : Any> right(value: RIGHT): FEither<Nothing, RIGHT> = Right(value)
        fun <LEFT : Any> left(value: LEFT): FEither<LEFT, Nothing> = Left(value)
    }
    data class Left<out LEFT>(val value: LEFT): FEither<LEFT, Nothing>()
    data class Right<out RIGHT>(val value: RIGHT): FEither<Nothing, RIGHT>()
}


fun <LEFT:Any, RIGHT:Any, OTHER:Any> FEither<LEFT, RIGHT>.map(block:(RIGHT)->OTHER): FEither<LEFT, OTHER> = when(this){
    is Left -> this
    is Right -> Right(block(value))
}
fun <LEFT:Any, RIGHT:Any, OTHER:Any> FEither<LEFT, RIGHT>.flatMap(block:(RIGHT)-> FEither<LEFT, OTHER>): FEither<LEFT, OTHER> = when(this){
    is Left -> this
    is Right -> block(value)
}
fun <LEFT:Any, RIGHT:Any> FEither<LEFT, RIGHT>.orElse(block:()-> FEither<LEFT, RIGHT>): FEither<LEFT, RIGHT> = when(this){
    is Left -> block()
    is Right -> this
}
fun <LEFT:Any, RIGHT1:Any, RIGHT2:Any, OTHER:Any> FEither<LEFT, RIGHT1>.map2(other: FEither<LEFT, RIGHT2>, block:(RIGHT1, RIGHT2)->OTHER): FEither<LEFT, OTHER>
= when(this){
    is Left -> this
    is Right -> when(other){
        is Left -> other
        is Right -> FEither.right(block(value, other.value))
    }
}
//= flatMap { v1->other.map {v2->block(v1, v2)} }
//fun <LEFT:Any, RIGHT1:Any, RIGHT2:Any, OTHER:Any> FEither<LEFT, RIGHT1>.map2Log(other: FEither<LEFT, RIGHT2>, block:(RIGHT1, RIGHT2)->OTHER): FEither<FList<LEFT>, OTHER>
//= when(this){
//    is Left ->when(other){
//        is Left -> Left(FList(value, other.value))
//        is Right -> Left(FList(value))
//    }
//    is Right ->when(other){
//        is Left -> Left(FList(other.value))
//        is Right -> Right(block(value, other.value))
//    }
//}
fun <LEFT:Any, RIGHT1:Any, RIGHT2:Any, OTHER:Any> FEither<LEFT, RIGHT1>.map2Log(other: FEither<FList<LEFT>, RIGHT2>, block:(RIGHT1, RIGHT2)->OTHER): FEither<FList<LEFT>, OTHER>
= when(this){
    is Left ->when(other){
        is Left -> Left(Cons(value, other.value))
        is Right -> Left(FList(value))
    }
    is Right ->when(other){
        is Left -> other
        is Right -> Right(block(value, other.value))
    }
}
fun <VALUE:Any, LEFT:Any, RIGHT:Any> FList<VALUE>.traverseEither(block:(VALUE)-> FEither<LEFT, RIGHT>): FEither<LEFT, FList<RIGHT>>
= foldRight(FEither.right(FList())){it, acc->block(it).map2(acc, ::Cons)}
inline fun <LEFT:Any, RIGHT:Any> FList<FEither<LEFT, RIGHT>>.sequenceEither(): FEither<LEFT, FList<RIGHT>>
= traverseEither{it}
fun <VALUE:Any, LEFT:Any, RIGHT:Any> FList<VALUE>.traverseEitherLog(block:(VALUE)-> FEither<LEFT, RIGHT>): FEither<FList<LEFT>, FList<RIGHT>>
= foldRight(FEither.right(FList())){it, acc->block(it).map2Log(acc, ::Cons)}
inline fun <LEFT:Any, RIGHT:Any> FList<FEither<LEFT, RIGHT>>.sequenceEitherLog(): FEither<FList<LEFT>, FList<RIGHT>>
= traverseEitherLog{it}
//= when(this){
//    is Nil -> FEither.right(FList())
//    is Cons ->{
//        val b = tail.traverseEither(block)
//        when(val a = block(head)){
//            is Left -> when(b){
//                is Left -> Left(Cons(a.value, b.value))
//                is Right -> Left(FList(a.value))
//            }
//            is Right ->when(b){
//                is Left -> Left(b.value)
//                is Right -> Right(Cons(a.value, b.value))
//            }
//        }
//    }
//}
//fun FList<Double>.averageEither(): Either<String, Double> = when(this){
//    is FList.Nil -> Either.Left("Empty list")
//    is FList.Cons -> Either.Right(sum() / size)
//}
//inline fun <LEFT:Any, RIGHT:Any> FList<Either<LEFT, RIGHT>>.sequenceEitherLog(): Either<FList<LEFT>, FList<RIGHT>>
//= traverseEitherLog{
//    when(it){
//        is Either.Left -> Either.Left(it.value)
//        is Either.Right -> Either.Right(it.value)
//    }
//}

//fun <VALUE:Any, LEFT:Any, RIGHT:Any> FList<VALUE>.traverseEither(block:(VALUE)-> Either<LEFT, RIGHT>): Either<LEFT, FList<RIGHT>>
//    = when(this){
//    is FList.Nil -> Either.right(FList())
//    is Cons ->when(val v = block(_head)){
//        is Either.Left ->v
//        is Either.Right ->v.map2(_tail.traverseEither(block), ::Cons)
//    }
//}
