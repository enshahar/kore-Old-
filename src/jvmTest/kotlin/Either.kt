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
    data class Left<out LEFT>(val value: LEFT):Either<LEFT, Nothing>()
    data class Right<out RIGHT>(val value: RIGHT):Either<Nothing, RIGHT>()
}

fun List<Double>.averageEither():Either<String, Double> = when(this){
    is List.Nil -> Either.Left("Empty list")
    is List.Cons -> Either.Right(sum() / size)
}
fun <LEFT:Any, RIGHT:Any, OTHER:Any> Either<LEFT, RIGHT>.map(block:(RIGHT)->OTHER):Either<LEFT, OTHER> = when(this){
    is Either.Left->this
    is Either.Right->Either.Right(block(value))
}
fun <LEFT:Any, RIGHT:Any, OTHER:Any> Either<LEFT, RIGHT>.flatMap(block:(RIGHT)->Either<LEFT, OTHER>):Either<LEFT, OTHER> = when(this){
    is Either.Left->this
    is Either.Right->block(value)
}
fun <LEFT:Any, RIGHT:Any> Either<LEFT, RIGHT>.orElse(block:()->Either<LEFT, RIGHT>):Either<LEFT, RIGHT> = when(this){
    is Either.Left->block()
    is Either.Right->this
}
fun <LEFT:Any, RIGHT1:Any, RIGHT2:Any, OTHER:Any> Either<LEFT, RIGHT1>.map2(other:Either<LEFT, RIGHT2>, block:(RIGHT1, RIGHT2)->OTHER):Either<LEFT, OTHER>
    = flatMap { v1->other.map {v2->block(v1, v2)} }