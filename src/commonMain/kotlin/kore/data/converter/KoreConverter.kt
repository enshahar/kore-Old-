@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package kore.data.converter

import kore.data.task.TaskStore
import kore.data.Data
import kore.data.Union
import kore.data.field.*
import kore.error.E
import kore.wrap.W
import kore.wrap.Wrap
import kotlin.reflect.KClass

/**
 * 문자열을 인코딩 디코딩할 때 이스케이프해야하는 특수문자 처리를 정의함
 * @ : 리스트와 맵의 종결자로 사용함
 * | : 모든 요소의 구분자로 사용함(리스트, 맵의 키와 값의 구분자, 엔티티 필드 구분자 등)
 * \n, \r : 문자열의 개행은 이스케이핑한다
 */
internal inline fun encodeString(v:Any?):String = "$v".replace("@","\\@").
replace("|","\\|").replace("~", "\\~").
replace("\n", "\\n").replace("\r", "\\\\r")
internal inline fun decodeString(v:String):String = v.replace("\\@","@").replace("\\|","|").replace("\\~","~").
replace("\\n", "\n").replace("\\r", "\r")

private const val optionalNullValue:Char = '~'
private const val emptyStringListValue = '!'
inline fun Data.encodeKore():Wrap<String> = KoreConverter.encode(this)
inline fun <DATA:Data> DATA.decodeKore(serial:String):Wrap<DATA> = KoreConverter.decode(this, serial)

@Suppress("NOTHING_TO_INLINE")
object KoreConverter: Converter<String> {
    fun setEncoder(type:KClass<*>,block:(Any, Field<*>)->Wrap<String>){
        KoreEncoder.encoders[type] = block
    }
    fun setDecoder(type:KClass<*>,block:(field: Field<*>, serial:String, cursor: KoreDecoder.Cursor)->Wrap<Any>){
        KoreDecoder.decoders[type] = block
    }
    override fun encode(data: Data):Wrap<String> = KoreEncoder.encodeData(data)
    override fun <DATA: Data> decode(data:DATA, value:String):Wrap<DATA> = KoreDecoder.decode(data, value)
}