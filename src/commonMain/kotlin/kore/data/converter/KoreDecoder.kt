package kore.data.converter

import kore.data.Data
import kore.data.field.*
import kore.error.E
import kore.wrap.Wrap
import kotlin.reflect.KClass

class Cursor(var v:Int)
/**
 * 디코더에서 사용하는 빈 객체
 * get()으로 값을 가져올 수 없음
 */
private val entry:Map.Entry<String, Field<*>> = object:Map.Entry<String, Field<*>>{
    override val key:String get() = throw Throwable("DecodeEntry")
    override val value: Field<*> get() = throw Throwable("DecodeEntry")
}
internal object KoreDecoder{
    /**
     * decodeStringValue 를 외부에 제공해주기 위한 함수
     */
    fun getDecodeStringValue(serial:String, cursor: Cursor):String = decodeStringValue(serial, cursor)
    private fun <DATA: Data> decodeEntity(serial:String, cursor: Cursor, entity:DATA, report: Report):DATA?{
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
            KoreConverter.entry
        )}}
        fields.forEach{
            convert[Indexer.get(type,it.key)] = it
        }
        convert.forEach{
            when{
                serial.length == cursor.v->{}
                serial[cursor.v] == KoreConverter.optionalNullValue -> cursor.v++
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
        if(serial[cursor.v] == KoreConverter.emptyStringListValue) {
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



    private inline fun decode(field: Field<*>, serial:String, cursor:Cursor):Any?{
        return decoders[field::class]?.invoke(field,serial,cursor,report)
    }
    fun <DATA> decode(data: DATA, value: String): Wrap<DATA> {
        TODO("Not yet implemented")
    }

    internal val decoders:HashMap<KClass<*>,(Field<*>, String, Cursor)-> Wrap<Any>> = hashMapOf(
        IntField::class to { f, s, c, r-> KoreConverter.decodeValue(s, c, r, String::toIntOrNull, f) },
        ShortField::class to { f, s, c, r-> KoreConverter.decodeValue(s, c, r, String::toShortOrNull, f) },
        LongField::class to { f, s, c, r-> KoreConverter.decodeValue(s, c, r, String::toLongOrNull, f) },
        UIntField::class to { f, s, c, r-> KoreConverter.decodeValue(s, c, r, String::toUIntOrNull, f) },
        UShortField::class to { f, s, c, r-> KoreConverter.decodeValue(s, c, r, String::toUShortOrNull, f) },
        ULongField::class to { f, s, c, r-> KoreConverter.decodeValue(s, c, r, String::toULongOrNull, f) },
        FloatField::class to { f, s, c, r-> KoreConverter.decodeValue(s, c, r, String::toFloatOrNull, f) },
        DoubleField::class to { f, s, c, r-> KoreConverter.decodeValue(s, c, r, String::toDoubleOrNull, f) },
        BooleanField::class to { f, s, c, r-> KoreConverter.decodeValue(s, c, r, String::toBooleanStrictOrNull, f) },
        IntListField::class to { _, s, c, r-> KoreConverter.decodeValueList(s, c, r, String::toIntOrNull) },
        ShortListField::class to { _, s, c, r-> KoreConverter.decodeValueList(s, c, r, String::toShortOrNull) },
        LongListField::class to { _, s, c, r-> KoreConverter.decodeValueList(s, c, r, String::toLongOrNull) },
        UIntListField::class to { _, s, c, r-> KoreConverter.decodeValueList(s, c, r, String::toUIntOrNull) },
        UShortListField::class to { _, s, c, r-> KoreConverter.decodeValueList(s, c, r, String::toUShortOrNull) },
        ULongListField::class to { _, s, c, r-> KoreConverter.decodeValueList(s, c, r, String::toULongOrNull) },
        FloatListField::class to { _, s, c, r-> KoreConverter.decodeValueList(s, c, r, String::toFloatOrNull) },
        DoubleListField::class to { _, s, c, r-> KoreConverter.decodeValueList(s, c, r, String::toDoubleOrNull) },
        BooleanListField::class to { _, s, c, r->
            KoreConverter.decodeValueList(
                s,
                c,
                r,
                String::toBooleanStrictOrNull
            )
        },
        IntMapField::class to { _, s, c, r-> KoreConverter.decodeValueMap(s, c, r, String::toIntOrNull) },
        ShortMapField::class to { _, s, c, r-> KoreConverter.decodeValueMap(s, c, r, String::toShortOrNull) },
        LongMapField::class to { _, s, c, r-> KoreConverter.decodeValueMap(s, c, r, String::toLongOrNull) },
        UIntMapField::class to { _, s, c, r-> KoreConverter.decodeValueMap(s, c, r, String::toUIntOrNull) },
        UShortMapField::class to { _, s, c, r-> KoreConverter.decodeValueMap(s, c, r, String::toUShortOrNull) },
        ULongMapField::class to { _, s, c, r-> KoreConverter.decodeValueMap(s, c, r, String::toULongOrNull) },
        FloatMapField::class to { _, s, c, r-> KoreConverter.decodeValueMap(s, c, r, String::toFloatOrNull) },
        DoubleMapField::class to { _, s, c, r-> KoreConverter.decodeValueMap(s, c, r, String::toDoubleOrNull) },
        BooleanMapField::class to { _, s, c, r-> KoreConverter.decodeValueMap(s, c, r, String::toBooleanStrictOrNull) },

        UtcField::class to { _, serial, cursor, _-> KoreConverter.decodeStringValue(serial, cursor).let{ eUtc.of(it) } },
        StringField::class to { _, serial, cursor, _-> KoreConverter.decodeStringValue(serial, cursor) },
        StringListField::class to { _, s, c, r-> KoreConverter.decodeStringList(s, c, r)?.map(this::decodeString)},
        StringMapField::class to fun(_, serial, cursor, report):Any?{
            var key:String? = null
            val result = hashMapOf<String,String>()
            KoreConverter.decodeStringList(serial, cursor, report)?.forEach{
                if(key == null) key = KoreConverter.decodeString(it)
                else{
                    result[key!!] = KoreConverter.decodeString(it)
                    key = null
                }
            } ?: return null
            return result
        },
        EnumField::class to fun(field, serial, cursor, report):Any?{
            val idx = KoreConverter.decodeValue(serial, cursor, report, String::toIntOrNull) ?:return null
            return (field as EnumField<*>).enums[idx]
        },
        EnumListField::class to fun(field, serial, cursor, report):Any?{
            val enums = (field as EnumListField<*>).enums
            val pin = cursor.v
            cursor.v = serial.indexOf("@",pin)
            if(cursor.v == -1) return report(Data.ERROR.decode_error,"invalid enumList. pin:${pin}")
            return serial.substring(pin,cursor.v++).split('|').map{enums[it.toInt()]}
        },
        EnumMapField::class to fun(field, serial, cursor, report):Any?{
            val enums = (field as EnumMapField<*>).enums
            var key:String? = null
            val result = hashMapOf<String,Any>()
            KoreConverter.decodeStringList(serial, cursor, report)?.forEach {
                if(key == null) key = KoreConverter.decodeString(it)
                else{
                    result[key!!] = enums[it.toIntOrNull()?:return report(Data.ERROR.decode_error,"invalid enumMap. it:${it}")]
                    key = null
                }
            } ?: return null
            return result
        },
        UnionField::class to{ field, serial, cursor, report->
            val pin = cursor.v
            val p = serial.indexOf('|',pin)
            cursor.v = if(p==-1) serial.length else p
            KoreConverter.decodeEntity(
                serial,
                cursor,
                (field as UnionField<*>).union.factories[serial.substring(pin, cursor.v++).toInt()](),
                report
            )
        },
        UnionListField::class to fun(field, serial, cursor, report):Any?{
            val result = arrayListOf<Any>()
            if(serial[cursor.v] == '@') cursor.v++
            else {
                val factories = (field as UnionListField<*>).union.factories
                do {
                    val pin = cursor.v
                    cursor.v = serial.indexOf('|',pin)
                    result += KoreConverter.decodeEntity(
                        serial,
                        cursor,
                        factories[serial.substring(pin, cursor.v++).toInt()](),
                        report
                    ) ?: return null
                    if(serial[cursor.v-1] == '@' && serial[cursor.v-2] != '\\') return result
                    when (serial[cursor.v++]) {
                        '|'->{}//next item
                        '@'->if (serial[cursor.v-2] != '\\') return result
                        else->return report(Data.ERROR.decode_error,"invalid unionList token:${serial[cursor.v-1]} / cursor:${cursor.v-1}")
                    }
                } while(true)
            }
            return result
        },
        UnionMapField::class to fun(field, serial, cursor, report):Any?{
            val result:HashMap<String, Data> = hashMapOf()
            if(serial[cursor.v] == '@') cursor.v++
            else{
                val factories = (field as UnionMapField<*>).union.factories
                var keyPin = cursor.v
                do {
                    when (serial[cursor.v++]) {
                        '|'->{
                            if (serial[cursor.v-2] != '\\') {
                                val key = KoreConverter.decodeString(serial.substring(keyPin, cursor.v - 1))
                                val pin = cursor.v
                                cursor.v = serial.indexOf('|',pin)
                                result[key] = KoreConverter.decodeEntity(
                                    serial,
                                    cursor,
                                    factories[serial.substring(pin, cursor.v++).toInt()](),
                                    report
                                ) ?: return null
                                when(serial[cursor.v++]){
                                    '|'-> keyPin = cursor.v
                                    '@'-> if (serial[cursor.v-2] != '\\') return result
                                    else-> return report(Data.ERROR.decode_error,"invalid unionMap token:${serial[cursor.v-1]} / cursor:${cursor.v-1}")
                                }
                            }
                        }
                    }
                } while(true)
            }
            return result
        },
        DataField::class to { field, serial, cursor, report->
            if(serial[cursor.v] == '|') ""
            else KoreConverter.decodeEntity(serial, cursor, (field as DataField<*>).factory(), report)
        },
        SlowEntityField::class to { field, serial, cursor, report->
            if(serial[cursor.v] == '|') ""
            else KoreConverter.decodeEntity(serial, cursor, (field as DataField<*>).factory(), report)
        },
        DataListField::class to fun(field, serial, cursor, report):Any?{
            val result = arrayListOf<Any>()
            if(serial[cursor.v] == '@') cursor.v++
            else{
                val factory = (field as DataListField<*>).factory
                do{
                    result += KoreConverter.decodeEntity(serial, cursor, factory(), report) ?:return null
                    if(cursor.v >= serial.length) return result
                    when(serial[cursor.v++]){
                        '|'->{}//next item
                        '@'->if (serial[cursor.v-2] != '\\') return result
                        else-> return report(Data.ERROR.decode_error,"invalid entityList token:${serial[cursor.v-1]} / cursor:${cursor.v-1}")
                    }
                } while(true)
            }
            return result
        },
        SlowEntityListField::class to fun(field, serial, cursor, report):Any?{
            val result = arrayListOf<Any>()
            if(serial[cursor.v] == '@') cursor.v++
            else{
                val factory = (field as DataListField<*>).factory
                do{
                    result += KoreConverter.decodeEntity(serial, cursor, factory(), report) ?:return null
                    if(cursor.v >= serial.length) return result
                    when(serial[cursor.v++]){
                        '|'->{}//next item
                        '@'->if (serial[cursor.v-2] != '\\') return result
                        else-> return report(Data.ERROR.decode_error,"invalid entityList token:${serial[cursor.v-1]} / cursor:${cursor.v-1}")
                    }
                } while(true)
            }
            return result
        },
        DataMapField::class to fun(field, serial, cursor, report):Any?{
            val result:HashMap<String, Data> = hashMapOf()
            if(serial[cursor.v] == '@') cursor.v++
            else{
                val factory = (field as DataMapField<*>).factory
                var pin = cursor.v
                do {
                    when (serial[cursor.v++]) {
                        '|'->{
                            if (serial[cursor.v-2] != '\\') {
                                val key = KoreConverter.decodeString(serial.substring(pin, cursor.v - 1))
                                result[key] = KoreConverter.decodeEntity(serial, cursor, factory(), report) ?:return null
                                when (serial[cursor.v++]) {
                                    '|'->pin = cursor.v
                                    '@'->return result
                                    else->return report(Data.ERROR.decode_error,"invalid entityMap token:${serial[cursor.v-1]} / cursor:${cursor.v-1}")
                                }
                            }
                        }
                    }
                } while(true)
            }
            return result
        },
        SlowEntityMapField::class to fun(field, serial, cursor, report):Any?{
            val result:HashMap<String, Data> = hashMapOf()
            if(serial[cursor.v] == '@') cursor.v++
            else{
                val factory = (field as DataMapField<*>).factory
                var pin = cursor.v
                do {
                    when (serial[cursor.v++]) {
                        '|'->{
                            if (serial[cursor.v-2] != '\\') {
                                val key = KoreConverter.decodeString(serial.substring(pin, cursor.v - 1))
                                result[key] = KoreConverter.decodeEntity(serial, cursor, factory(), report) ?:return null
                                when (serial[cursor.v++]) {
                                    '|'->pin = cursor.v
                                    '@'->return result
                                    else->return report(Data.ERROR.decode_error,"invalid entityMap token:${serial[cursor.v-1]} / cursor:${cursor.v-1}")
                                }
                            }
                        }
                    }
                } while(true)
            }
            return result
        }
    )
}