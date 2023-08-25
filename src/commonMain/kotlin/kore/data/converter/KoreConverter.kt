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
inline fun Data.encodeKore():Wrap<String> = KoreConverter.encode(this)
inline fun <DATA:Data> DATA.decodeKore(serial:String):Wrap<DATA> = KoreConverter.decode(this, serial)

@Suppress("NOTHING_TO_INLINE")
object KoreConverter: Converter<String> {
    private const val optionalNullValue:Char = '~'
    private const val emptyStringListValue = '!'
    class Cursor(var v:Int)
    fun setEncoder(type:KClass<*>,block:(Any, Field<*>)->Wrap<String>){
        KoreEncoder.encoders[type] = block
    }
    fun setDecoder(type:KClass<*>,block:(field: Field<*>, serial:String, cursor: Cursor)->Wrap<Any>){
        KoreDecoder.decoders[type] = block
    }
    override fun encode(data: Data):Wrap<String> = KoreEncoder.encodeData(data)
    override fun <DATA: Data> decode(data:DATA, value:String):Wrap<DATA> = KoreDecoder.decode(data, value)



    /**
     * decodeStringValue 를 외부에 제공해주기 위한 함수
     */
    fun getDecodeStringValue(serial:String, cursor: Cursor):String = decodeStringValue(serial, cursor)

    /**
     * 디코더에서 사용하는 빈 객체
     * get()으로 값을 가져올 수 없음
     */
    private val entry:Map.Entry<String, Field<*>> = object:Map.Entry<String, Field<*>>{
        override val key:String get() = throw E(Data.ERROR.encode_error,"")
        override val value: Field<*> get() = throw E(Data.ERROR.encode_error,"")
    }





    /**
     * Decode-------------------------------
     */
    private inline fun <ENTITY: Data> decodeEntity(serial:String, cursor: Cursor, entity:ENTITY, report: Report):ENTITY?{
        val type:KClass<out Data> = entity::class
        //val fields:HashMap<String,Field<*>> = Field[type] ?: return entity
        val fields:HashMap<String, Field<*>> = entity.fields

        if(entity.fields.isEmpty()) {
            if(serial[cursor.v++] == '|') {
                return entity
            } else {
                return report(Data.ERROR.decode_error,"empty entity expected at ${cursor.v-1}:${type.simpleName}")
            }
        }

        val convert:ArrayList<Map.Entry<String, Field<*>>> = ArrayList<Map.Entry<String, Field<*>>>(fields.size).also{ list->repeat(fields.size){list.add(
            entry
        )}}
        fields.forEach{
            convert[Indexer.get(type,it.key)] = it
        }
        convert.forEach{
            when{
                serial.length == cursor.v->{}
                serial[cursor.v] == optionalNullValue -> cursor.v++
                else->{
                    val v = decode(it.value, serial, cursor, report) ?: return report(Data.ERROR.decode_error,"no value:${type.simpleName}:${it.key}")
                    try{
                        entity.setRawValue(it.key, v)
                    }catch(e: E){
                        return report(e.id, e.message, *e.result)
                    }
                }
            }
            cursor.v++
        }
        return entity
    }
    private inline fun<T> decodeValue(serial:String, cursor: Cursor, report: Report, block:String.()->T?, field: Field<*>? = null):T?{
        val start = cursor.v
        cursor.v = serial.indexOfAny(charArrayOf('|','@'),start)
        if(cursor.v == -1) cursor.v = serial.length
        val chunk = serial.substring(start,cursor.v)
        return chunk.block() ?:return report(Data.ERROR.decode_error,"invalid type.field:$field,chunk:*$chunk*,start:$start,cursor:${cursor.v},serial:$serial")
    }
    private inline fun <T> decodeValueList(serial:String, cursor: Cursor, report: Report, block:String.()->T?):Any?{
        val pin = cursor.v
        cursor.v = serial.indexOf("@",pin)
        // '
        return serial.substring(pin,cursor.v++).split('|').mapIndexed{index,it->
            it.block() ?:return report(Data.ERROR.decode_error,"invalid type. $it,index:$index")
        }
    }
    private inline fun <T> decodeValueMap(serial:String, cursor: Cursor, report:Report, block:String.()->T?):Any?{
        var key:String? = null
        val result = hashMapOf<String,T>()
        decodeStringList(serial,cursor,report)?.forEach{
            if(key == null) key = decodeString(it)
            else{
                result[key!!] = it.block() ?: return report(Data.ERROR.decode_error,"invalid type. $it,key:$key")
                key = null
            }
        }?:return null
        return result
    }
    private val regStringSplit = """(?<!\\)\|""".toRegex()
    private inline fun decodeStringList(serial:String, cursor: Cursor, report:Report):List<String>?{
        // 빈 문자열 리스트는 특별처리를 해야 한다
        // 안 그러면 빈 문자열로 이뤄진 리스트와 아예 빈 리스트를 `|`만으로 100% 확신하면서 파싱할 수 없다.
        if(serial[cursor.v] == emptyStringListValue) {
            cursor.v++
            return listOf<String>()
        }

        val pin = cursor.v
        var at = pin
        do{
            cursor.v = serial.indexOf("@",at)
            if(cursor.v == -1)
                return report(Data.ERROR.decode_error,"invalid stringList. pin:${pin}")
            if(serial[cursor.v-1] == '\\') at = cursor.v+1
            else break
        } while(true)
        return serial.substring(pin,cursor.v++).let{
            it.split(regStringSplit)
        }
    }
    private inline fun decodeStringValue(serial:String, cursor: Cursor):String{
        // 문자열은 필드로 들어간 경우만 처리하면 된다.
        // 리스트 원소로 들어갈 때는 decodeStringList에서 처리됨
        // 따라서 @를 만나면 오류임
        val pin = cursor.v
        var pos = pin
        do {
            pos = serial.indexOf('|', pos)
            if (pos == -1) {
                // serial 맨 끝까지 문자열인 경우
                cursor.v = serial.length
                return decodeString(serial.substring(pin,cursor.v))
            } else if(pos==pin) {
                // 길이 0인 문자열
                cursor.v
                return ""
            } else {
                // \| 스킵하기
                if (serial[pos-1] == '\\')
                    pos++
                else {
                    cursor.v = pos
                    return decodeString(serial.substring(pin, pos))
                }
            }
        } while(true)
    }



    private inline fun decode(field: Field<*>, serial:String, cursor: Cursor, report: Report):Any?{
        return decoders[field::class]?.invoke(field,serial,cursor,report)
    }
}