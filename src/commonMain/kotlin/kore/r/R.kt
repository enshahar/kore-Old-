package kore.r

import kotlin.jvm.JvmInline

/**
 * 성공, 실패를 내포하는 결과값 보고 객체. 최초 값을 람다로 설정하면 이후 모든 map연산이 지연연산으로 처리됨
 */
@JvmInline
@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
value class R<out VALUE:Any> @PublishedApi internal constructor(@PublishedApi internal val value:Any){
    companion object{
        /** 정상인 값을 생성함 */
        inline fun <VALUE:Any>ok(value:VALUE):R<VALUE> = R(value)
        /** 정상인 값을 람다로 생성함. 이후 모든 처리는 지연연산으로 처리되고 invoke시점까지 평가가 미뤄짐 */
        inline fun <VALUE:Any>ok(noinline f:()->VALUE):R<VALUE> = R(f)
        /** 실패인 값을 예외객체로 생성함*/
        inline fun <VALUE:Any>fail(fail:Throwable):R<VALUE> = R(fail)
        inline fun failInt(fail:Throwable):R<Int> = R(fail)
        inline fun failShort(fail:Throwable):R<Short> = R(fail)
        inline fun failLong(fail:Throwable):R<Long> = R(fail)
        inline fun failFloat(fail:Throwable):R<Float> = R(fail)
        inline fun failDouble(fail:Throwable):R<Double> = R(fail)
        inline fun failBoolean(fail:Throwable):R<Boolean> = R(fail)
        inline fun failString(fail:Throwable):R<String> = R(fail)
    }
    /** R타입을 유지한 상태로 내부의 상태를 바꾸는 연산. 지연연산 모드에서는 계속 지연함수합성이 됨.
     *  map에 전달되는 람다는 throw할 수 있으며 이를 통해 fail상태로 이전시킬 수 있음.
     */
    inline fun <OTHER:Any> map(crossinline block:(VALUE)->OTHER):R<OTHER> = when(value){
        is Throwable -> this as R<OTHER>
        is Function0<*> -> R{block((value as ()->VALUE)())}
        else -> R(block(value as VALUE))
    }
    inline fun <OTHER:Any> mapLazy(crossinline block:(VALUE)->OTHER):R<OTHER> = when(value){
        is Throwable -> this as R<OTHER>
        is Function0<*> -> R{block((value as ()->VALUE)())}
        else -> R{block(value as VALUE)}
    }
    /** 실패값을 반드시 복원할 수 있는 정책이 있는 경우 복원용 람다를 통해 현재 상태를 나타내는 예외로부터 값을 만들어냄 */
    inline operator fun invoke(block:(Throwable)-> @UnsafeVariance VALUE):VALUE = when(value) {
        is Throwable -> block(value)
        is Function0<*> ->try {
            (value as () -> VALUE)()
        }catch (e:Throwable){
            block(e)
        }
        else -> value as VALUE
    }
    /** 정상인 값은 반환되지만 비정상인 값은 null이 됨. 지연연산 설정 시 이 시점에 해소됨*/
    inline operator fun invoke():VALUE? = when(value){
        is Throwable -> null
        is Function0<*> ->try {
            (value as () -> VALUE)()
        }catch (_:Throwable){
            null
        }
        else -> value as VALUE
    }
}