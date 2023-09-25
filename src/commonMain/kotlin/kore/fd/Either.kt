package kore.fd

import kore.fd.FList.Cons

sealed class Either<out LEFT, out RIGHT> {
    companion object {
        fun <RIGHT : Any> catches(block: () -> RIGHT): Either<Throwable, RIGHT> = try {
            Right(block())
        } catch (e: Throwable) {
            Left(e)
        }
        fun <RIGHT : Any> right(value: RIGHT): Either<Nothing, RIGHT> = Right(value)
        fun <LEFT : Any> left(value: LEFT): Either<LEFT, Nothing> = Left(value)
    }
    data class Left<out LEFT>(val value: LEFT): Either<LEFT, Nothing>()
    data class Right<out RIGHT>(val value: RIGHT): Either<Nothing, RIGHT>()
}

//fun FList<Double>.averageEither(): Either<String, Double> = when(this){
//    is FList.Nil -> Either.Left("Empty list")
//    is FList.Cons -> Either.Right(sum() / size)
//}
fun <LEFT:Any, RIGHT:Any, OTHER:Any> Either<LEFT, RIGHT>.map(block:(RIGHT)->OTHER): Either<LEFT, OTHER> = when(this){
    is Either.Left ->this
    is Either.Right -> Either.Right(block(value))
}
fun <LEFT:Any, RIGHT:Any, OTHER:Any> Either<LEFT, RIGHT>.flatMap(block:(RIGHT)-> Either<LEFT, OTHER>): Either<LEFT, OTHER> = when(this){
    is Either.Left ->this
    is Either.Right ->block(value)
}
fun <LEFT:Any, RIGHT:Any> Either<LEFT, RIGHT>.orElse(block:()-> Either<LEFT, RIGHT>): Either<LEFT, RIGHT> = when(this){
    is Either.Left ->block()
    is Either.Right ->this
}
fun <LEFT:Any, RIGHT1:Any, RIGHT2:Any, OTHER:Any> Either<LEFT, RIGHT1>.map2(other: Either<LEFT, RIGHT2>, block:(RIGHT1, RIGHT2)->OTHER): Either<LEFT, OTHER>
    = flatMap { v1->other.map {v2->block(v1, v2)} }
fun <LEFT:Any, RIGHT1:Any, RIGHT2:Any, OTHER:Any> Either<LEFT, RIGHT1>.map2Log(other: Either<LEFT, RIGHT2>, block:(RIGHT1, RIGHT2)->OTHER): Either<FList<LEFT>, OTHER>
    = when(this){
        is Either.Left ->when(other){
            is Either.Left -> Either.Left(FList(value, other.value))
            is Either.Right -> Either.Left(FList(value))
        }
        is Either.Right ->when(other){
            is Either.Left -> Either.Left(FList(other.value))
            is Either.Right -> Either.Right(block(value, other.value))
        }
    }
//inline fun <LEFT:Any, RIGHT:Any> FList<Either<LEFT, RIGHT>>.sequenceEitherLog(): Either<FList<LEFT>, FList<RIGHT>>
//= traverseEitherLog{
//    when(it){
//        is Either.Left -> Either.Left(it.value)
//        is Either.Right -> Either.Right(it.value)
//    }
//}
//fun <VALUE:Any, LEFT:Any, RIGHT:Any> FList<VALUE>.traverseEitherLog(block:(VALUE)-> Either<LEFT, RIGHT>): Either<FList<LEFT>, FList<RIGHT>>
// = when(this){
//    is FList.Nil -> Either.right(FList())
//    is Cons ->{
//        val b = _tail.traverseEitherLog(block)
//        when(val a = block(_head)){
//            is Either.Left ->when(b){
//                is Either.Left -> Either.Left(Cons(a.value, b.value))
//                is Either.Right -> Either.Left(FList(a.value))
//            }
//            is Either.Right ->when(b){
//                is Either.Left -> Either.Left(b.value)
//                is Either.Right -> Either.Right(Cons(a.value, b.value))
//            }
//        }
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
//inline fun <LEFT:Any, RIGHT:Any> FList<Either<LEFT, RIGHT>>.sequenceEither(): Either<LEFT, FList<RIGHT>>
//    = traverseEither{it}