@file:Suppress("NOTHING_TO_INLINE")

package kore.fd

import kore.fd.FEither.Left
import kore.fd.FEither.Right
import kore.fd.FList.Cons
import kore.fd.FList.Nil

sealed class FEither<out L, out R>{
    data class Left<out L> @PublishedApi internal constructor(val value: L):FEither<L, Nothing>()
    data class Right<out R> @PublishedApi internal constructor(val value: R):FEither<Nothing, R>()
    companion object{
        inline fun <R:Any> catches(throwBlock:()->R):FEither<Throwable, R>
        = try{
            Right(throwBlock())
        }catch(e:Throwable){
            Left(e)
        }
        inline fun <L:Any, R:Any> right(value:R):FEither<L, R> = Right(value)
        inline fun <L:Any, R:Any> left(value:L):FEither<L, R> = Left(value)
    }
}
fun <L:Any, R:Any, OTHER:Any> FEither<L, R>.map(block:(R)->OTHER):FEither<L, OTHER>
= when(this){
    is Left -> this
    is Right -> Right(block(value))
}
fun <L:Any, R:Any, OTHER:Any> FEither<L, R>.flatMap(block:(R)->FEither<L, OTHER>):FEither<L, OTHER>
= when(this){
    is Left -> this
    is Right -> block(value)
}
fun <L:Any, R:Any> FEither<L, R>.orElse(block:()->FEither<L, R>):FEither<L, R>
= when(this){
    is Left -> block()
    is Right -> this
}
fun <L:Any, R1:Any, R2:Any, OTHER:Any> FEither<L, R1>.map2(other:FEither<L, R2>, block:(R1, R2)->OTHER):FEither<L, OTHER>
= when(this){
    is Left -> this
    is Right -> when(other){
        is Left -> other
        is Right -> Right(block(value, other.value))
    }
}
//= flatMap { v1->other.map {v2->block(v1, v2)} }
//fun <L:Any, R1:Any, R2:Any, OTHER:Any> FEither<L, R1>.map2Log(other: FEither<L, R2>, block:(R1, R2)->OTHER): FEither<FList<L>, OTHER>
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
fun <L:Any, R1:Any, R2:Any, OTHER:Any> FEither<L, R1>.map2Log(other: FEither<FList<L>, R2>, block:(R1, R2)->OTHER): FEither<FList<L>, OTHER>
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
fun <VALUE:Any, L:Any, R:Any> FList<VALUE>.traverseEither(block:(VALUE)-> FEither<L, R>): FEither<L, FList<R>>
= foldRight(FEither.right(FList())){it, acc->block(it).map2(acc, ::Cons)}
inline fun <L:Any, R:Any> FList<FEither<L, R>>.sequenceEither(): FEither<L, FList<R>>
= traverseEither{it}
fun <VALUE:Any, L:Any, R:Any> FList<VALUE>.traverseEitherLog(block:(VALUE)-> FEither<L, R>): FEither<FList<L>, FList<R>>
= foldRight(FEither.right(FList())){it, acc->block(it).map2Log(acc, ::Cons)}
inline fun <L:Any, R:Any> FList<FEither<L, R>>.sequenceEitherLog(): FEither<FList<L>, FList<R>>
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
//inline fun <L:Any, R:Any> FList<Either<L, R>>.sequenceEitherLog(): Either<FList<L>, FList<R>>
//= traverseEitherLog{
//    when(it){
//        is Either.Left -> Either.Left(it.value)
//        is Either.Right -> Either.Right(it.value)
//    }
//}

//fun <VALUE:Any, L:Any, R:Any> FList<VALUE>.traverseEither(block:(VALUE)-> Either<L, R>): Either<L, FList<R>>
//    = when(this){
//    is FList.Nil -> Either.right(FList())
//    is Cons ->when(val v = block(_head)){
//        is Either.Left ->v
//        is Either.Right ->v.map2(_tail.traverseEither(block), ::Cons)
//    }
//}
