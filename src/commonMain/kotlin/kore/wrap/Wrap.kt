@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package kore.wrap

import kotlin.jvm.JvmInline

@JvmInline
value class Wrap<out VALUE:Any> @PublishedApi internal constructor(@PublishedApi internal val value:Any){

    /** R타입을 유지한 상태로 내부의 상태를 바꾸는 연산. 지연연산 모드에서는 계속 지연함수합성이 됨.
     *  map에 전달되는 람다는 throw할 수 있으며 이를 통해 fail상태로 이전시킬 수 있음.
     */
    inline fun <OTHER:Any> map(crossinline block:(VALUE)->OTHER):Wrap<OTHER> = when(value){
        is Throwable -> this as Wrap<OTHER>
        is Function0<*> -> try{Wrap{block(value.invoke() as VALUE)}}catch (e:Throwable){Wrap(e)}
        else -> Wrap(block(value as VALUE))
    }
    /** 최초의 값을 람다로 지정하지 않아도 이후 지연연산하게 변경함*/
    inline fun <OTHER:Any> mapLazy(crossinline block:(VALUE)->OTHER):Wrap<OTHER> = when(value){
        is Throwable -> this as Wrap<OTHER>
        is Function0<*> -> Wrap{block(value.invoke() as VALUE)}
        else -> Wrap{block(value as VALUE)}
    }
    /** 실패값을 반드시 복원할 수 있는 정책이 있는 경우 복원용 람다를 통해 현재 상태를 나타내는 예외로부터 값을 만들어냄 */
    inline operator fun invoke(block:(Throwable)-> @UnsafeVariance VALUE):VALUE = when(value) {
        is Throwable -> block(value)
        is Function0<*> ->try {
                value.invoke()
            }catch (e:Throwable){
                block(e)
            }
        else -> value
    } as VALUE
    /** 정상인 값은 반환되지만 비정상인 값은 null이 됨. 지연연산 설정 시 이 시점에 해소됨*/
    inline operator fun invoke():VALUE? = when(value){
        is Throwable -> null
        is Function0<*> ->try {
                value.invoke() as VALUE
            }catch (_:Throwable){
                null
            }
        else -> value as VALUE
    }
    @JvmInline
    value class OrFail(val throwable:Throwable?){
        inline infix fun orFail(block:(Throwable)->Unit):Boolean{
            return throwable?.let{
                block(it)
            } == null
        }
    }
    inline fun get(block:(VALUE)->Unit):OrFail{
        val v = when(value){
            is Throwable -> value
            is Function0<*> ->try {
                value.invoke() as VALUE
            }catch (e:Throwable){
                e
            }
            else -> value as VALUE
        }
        return if(v is Throwable) OrFail(v) else{
            block(v as VALUE)
            OrFail(null)
        }
    }
}