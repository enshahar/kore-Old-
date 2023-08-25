@file:Suppress("NOTHING_TO_INLINE")

package kore.data.converter

import kore.data.Data
import kore.data.SlowData
import kore.data.converter.KoreConverter.Cursor
import kore.data.converter.KoreConverter.OPTIONAL_NULL_C
import kore.data.converter.KoreConverter.decodeString
import kore.data.field.*
import kore.data.indexer.Indexer
import kore.error.E
import kore.wrap.W
import kore.wrap.Wrap
import kotlin.reflect.KClass


/**
 * 디코더에서 사용하는 빈 객체
 * get()으로 값을 가져올 수 없음
 */
private val entry:Map.Entry<String, Field<*>> = object:Map.Entry<String, Field<*>>{
    override val key:String get() = throw Throwable("DecodeEntry")
    override val value: Field<*> get() = throw Throwable("DecodeEntry")
}
//fun getDecodeStringValue(cursor: Cursor):String = KoreDecoder.decodeStringValue(cursor)
internal object KoreDecoder{
    class DecodeInvalidEmptyData(val data:Data, val cursor:Int): E(data)
    class DecodeNoDecoder(val field:Field<*>, val cursor:Int, val encoded:String): E(field, cursor, encoded)
    class DecodeInvalidValue(val field:Field<*>, val target:String, val cursor:Int): E(field, target, cursor)
    class DecodeInvalidListValue(val field:Field<*>, val target:String, val index:Int): E(field, target, index)
    class DecodeInvalidMapValue(val field:Field<*>, val target:String, val key:String): E(field, target, key)
    private inline fun decode(cursor:Cursor, field: Field<*>):Wrap<Any>{
        return decoders[field::class]?.invoke(cursor, field) ?: W(DecodeNoDecoder(field, cursor.v, cursor.encoded))
    }
    internal fun <DATA: Data> decodeData(cursor:Cursor, data:DATA):Wrap<DATA>{
        val type:KClass<out Data> = data::class
        val slowData: SlowData? = data as? SlowData
        /** 이미 data의 인스턴스를 받았으므로 slow의 _fields나 Field에 있을 수 밖에 없음*/
        val fields:HashMap<String, Field<*>> = slowData?._fields ?: Field[data::class]!!
        if(fields.isEmpty()){
            return if(cursor.getAndNext() == '|') W(data) else W(DecodeInvalidEmptyData(data, cursor.v - 1))
        }
        val convert: ArrayList<Map.Entry<String, Field<*>>> = ArrayList(fields.size)
        repeat(fields.size){convert.add(entry)}
        fields.forEach{convert[Indexer.get(type, it.key)()!!] = it}
        convert.forEach{entry->
            when{
                cursor.isEnd->{}
                cursor.curr == OPTIONAL_NULL_C -> cursor.v++
                else-> decode(cursor, entry.value).get{
                    try{
                        data.setRawValue(entry.key, it)
                    }catch(e:Throwable){
                        return W(e)
                    }
                } orFail {
                    return W(it)
                }
            }
            cursor.v++
        }
        return W(data)
    }
    private inline fun<VALUE:Any> value(cursor:Cursor, field: Field<*>, block:String.()->VALUE?):Wrap<VALUE>{
        val target: String = cursor.nextValue
        return block(target)?.let{ W(it) } ?: W(DecodeInvalidValue(field, target, cursor.v))
    }
    private inline fun <VALUE:Any> valueList(cursor: Cursor, field: Field<*>, block:String.()->VALUE?):Wrap<List<VALUE>>{
        return W(cursor.nextValueList.mapIndexed{index,it->
            it.block() ?: return W(DecodeInvalidListValue(field, it, index))
        })
    }
    private inline fun <VALUE:Any> valueMap(cursor:Cursor, field: Field<*>, block:String.()->VALUE?):Wrap<HashMap<String, VALUE>>{
        var key:String? = null
        return W(cursor.nextValueList.fold(hashMapOf()){acc, it->
            if(key == null) key = decodeString(it)
            else{
                acc[key!!] = it.block() ?: return W(DecodeInvalidMapValue(field, it, key!!))
                key = null
            }
            acc
        })
    }
    private inline fun stringValue(cursor: Cursor):Wrap<String>{
        /** 문자열은 필드로 들어간 경우만 처리하면 된다.
         *  리스트 원소로 들어갈 때는 stringList에서 처리됨
         *  따라서 @를 만나면 오류임
          */
        val encoded = cursor.encoded
        val pin = cursor.v
        var pos = pin
        do {
            pos = encoded.indexOf('|', pos)
            if (pos == -1) { /** encoded 맨 끝까지 문자열인 경우 */
                cursor.v = encoded.length
                return W(decodeString(encoded.substring(pin, cursor.v)))
            } else if(pos == pin){ /** 길이 0인 문자열 */
                cursor.v
                return W("")
            } else { /** \| 스킵하기 */
                if (encoded[pos-1] == '\\') pos++
                else {
                    cursor.v = pos
                    return W(decodeString(encoded.substring(pin, pos)))
                }
            }
        } while(true)
    }
//    private val regStringSplit = """(?<!\\)\|""".toRegex()
//    private inline fun decodeStringList(cursor: Cursor):List<String>?{
//        // 빈 문자열 리스트는 특별처리를 해야 한다
//        // 안 그러면 빈 문자열로 이뤄진 리스트와 아예 빈 리스트를 `|`만으로 100% 확신하면서 파싱할 수 없다.
//        if(serial[cursor.v] == KoreConverter.STRINGLIST_EMPTY) {
//            cursor.v++
//            return listOf<String>()
//        }
//
//        val pin = cursor.v
//        var at = pin
//        do{
//            cursor.v = serial.indexOf("@",at)
//            if(cursor.v == -1)
//                return report(Data.ERROR.decode_error,"invalid stringList. pin:${pin}")
//            if(serial[cursor.v-1] == '\\') at = cursor.v+1
//            else break
//        } while(true)
//        return serial.substring(pin,cursor.v++).let{
//            it.split(regStringSplit)
//        }
//    }

    internal val decoders:HashMap<KClass<*>,(Cursor, Field<*>)->Wrap<Any>> = hashMapOf(
        IntField::class to { c, f->value(c, f){toIntOrNull()}},
        ShortField::class to { c, f->value(c, f){toShortOrNull()}},
        LongField::class to { c, f->value(c, f){toLongOrNull()}},
        UIntField::class to { c, f->value(c, f){toUIntOrNull()}},
        UShortField::class to { c, f->value(c, f){toUShortOrNull()}},
        ULongField::class to { c, f->value(c, f){toULongOrNull()}},
        FloatField::class to { c, f->value(c, f){toFloatOrNull()}},
        DoubleField::class to { c, f->value(c, f){toDoubleOrNull()}},
        BooleanField::class to { c, f->value(c, f){toBooleanStrictOrNull()}},
        IntListField::class to { c, f->valueList(c, f){toIntOrNull()}},
        ShortListField::class to { c, f->valueList(c, f){toUShortOrNull()}},
        LongListField::class to { c, f->valueList(c, f){toLongOrNull()}},
        UIntListField::class to { c, f->valueList(c, f){toUIntOrNull()}},
        UShortListField::class to { c, f->valueList(c, f){toUShortOrNull()}},
        ULongListField::class to { c, f->valueList(c, f){toULongOrNull()}},
        FloatListField::class to { c, f->valueList(c, f){toFloatOrNull()}},
        DoubleListField::class to { c, f->valueList(c, f){toDoubleOrNull()}},
        BooleanListField::class to { c, f->valueList(c, f){toBooleanStrictOrNull()}},
        IntMapField::class to { c, f->valueMap(c, f){toIntOrNull()}},
        ShortMapField::class to { c, f->valueMap(c, f){toUShortOrNull()}},
        LongMapField::class to { c, f->valueMap(c, f){toLongOrNull()}},
        UIntMapField::class to { c, f->valueMap(c, f){toUIntOrNull()}},
        UShortMapField::class to { c, f->valueMap(c, f){toUShortOrNull()}},
        ULongMapField::class to { c, f->valueMap(c, f){toULongOrNull()}},
        FloatMapField::class to { c, f->valueMap(c, f){toFloatOrNull()}},
        DoubleMapField::class to { c, f->valueMap(c, f){toDoubleOrNull()}},
        BooleanMapField::class to { c, f->valueMap(c, f){toBooleanStrictOrNull()}},
//        UtcField::class to { _, serial, cursor, _-> KoreConverter.decodeStringValue(serial, cursor).let{ eUtc.of(it) } },
        StringField::class to {c, f->stringValue(c) },
//        StringListField::class to { _, s, c, r-> KoreConverter.decodeStringList(s, c, r)?.map(this::decodeString)},
//        StringMapField::class to fun(_, serial, cursor, report):Any?{
//            var key:String? = null
//            val result = hashMapOf<String,String>()
//            KoreConverter.decodeStringList(serial, cursor, report)?.forEach{
//                if(key == null) key = KoreConverter.decodeString(it)
//                else{
//                    result[key!!] = KoreConverter.decodeString(it)
//                    key = null
//                }
//            } ?: return null
//            return result
//        },
//        EnumField::class to fun(field, serial, cursor, report):Any?{
//            val idx = KoreConverter.decodeValue(serial, cursor, report, String::toIntOrNull) ?:return null
//            return (field as EnumField<*>).enums[idx]
//        },
//        EnumListField::class to fun(field, serial, cursor, report):Any?{
//            val enums = (field as EnumListField<*>).enums
//            val pin = cursor.v
//            cursor.v = serial.indexOf("@",pin)
//            if(cursor.v == -1) return report(Data.ERROR.decode_error,"invalid enumList. pin:${pin}")
//            return serial.substring(pin,cursor.v++).split('|').map{enums[it.toInt()]}
//        },
//        EnumMapField::class to fun(field, serial, cursor, report):Any?{
//            val enums = (field as EnumMapField<*>).enums
//            var key:String? = null
//            val result = hashMapOf<String,Any>()
//            KoreConverter.decodeStringList(serial, cursor, report)?.forEach {
//                if(key == null) key = KoreConverter.decodeString(it)
//                else{
//                    result[key!!] = enums[it.toIntOrNull()?:return report(Data.ERROR.decode_error,"invalid enumMap. it:${it}")]
//                    key = null
//                }
//            } ?: return null
//            return result
//        },
//        UnionField::class to{ field, serial, cursor, report->
//            val pin = cursor.v
//            val p = serial.indexOf('|',pin)
//            cursor.v = if(p==-1) serial.length else p
//            KoreConverter.decodeEntity(
//                serial,
//                cursor,
//                (field as UnionField<*>).union.factories[serial.substring(pin, cursor.v++).toInt()](),
//                report
//            )
//        },
//        UnionListField::class to fun(field, serial, cursor, report):Any?{
//            val result = arrayListOf<Any>()
//            if(serial[cursor.v] == '@') cursor.v++
//            else {
//                val factories = (field as UnionListField<*>).union.factories
//                do {
//                    val pin = cursor.v
//                    cursor.v = serial.indexOf('|',pin)
//                    result += KoreConverter.decodeEntity(
//                        serial,
//                        cursor,
//                        factories[serial.substring(pin, cursor.v++).toInt()](),
//                        report
//                    ) ?: return null
//                    if(serial[cursor.v-1] == '@' && serial[cursor.v-2] != '\\') return result
//                    when (serial[cursor.v++]) {
//                        '|'->{}//next item
//                        '@'->if (serial[cursor.v-2] != '\\') return result
//                        else->return report(Data.ERROR.decode_error,"invalid unionList token:${serial[cursor.v-1]} / cursor:${cursor.v-1}")
//                    }
//                } while(true)
//            }
//            return result
//        },
//        UnionMapField::class to fun(field, serial, cursor, report):Any?{
//            val result:HashMap<String, Data> = hashMapOf()
//            if(serial[cursor.v] == '@') cursor.v++
//            else{
//                val factories = (field as UnionMapField<*>).union.factories
//                var keyPin = cursor.v
//                do {
//                    when (serial[cursor.v++]) {
//                        '|'->{
//                            if (serial[cursor.v-2] != '\\') {
//                                val key = KoreConverter.decodeString(serial.substring(keyPin, cursor.v - 1))
//                                val pin = cursor.v
//                                cursor.v = serial.indexOf('|',pin)
//                                result[key] = KoreConverter.decodeEntity(
//                                    serial,
//                                    cursor,
//                                    factories[serial.substring(pin, cursor.v++).toInt()](),
//                                    report
//                                ) ?: return null
//                                when(serial[cursor.v++]){
//                                    '|'-> keyPin = cursor.v
//                                    '@'-> if (serial[cursor.v-2] != '\\') return result
//                                    else-> return report(Data.ERROR.decode_error,"invalid unionMap token:${serial[cursor.v-1]} / cursor:${cursor.v-1}")
//                                }
//                            }
//                        }
//                    }
//                } while(true)
//            }
//            return result
//        },
//        DataField::class to { field, serial, cursor, report->
//            if(serial[cursor.v] == '|') ""
//            else KoreConverter.decodeEntity(serial, cursor, (field as DataField<*>).factory(), report)
//        },
//        SlowEntityField::class to { field, serial, cursor, report->
//            if(serial[cursor.v] == '|') ""
//            else KoreConverter.decodeEntity(serial, cursor, (field as DataField<*>).factory(), report)
//        },
//        DataListField::class to fun(field, serial, cursor, report):Any?{
//            val result = arrayListOf<Any>()
//            if(serial[cursor.v] == '@') cursor.v++
//            else{
//                val factory = (field as DataListField<*>).factory
//                do{
//                    result += KoreConverter.decodeEntity(serial, cursor, factory(), report) ?:return null
//                    if(cursor.v >= serial.length) return result
//                    when(serial[cursor.v++]){
//                        '|'->{}//next item
//                        '@'->if (serial[cursor.v-2] != '\\') return result
//                        else-> return report(Data.ERROR.decode_error,"invalid entityList token:${serial[cursor.v-1]} / cursor:${cursor.v-1}")
//                    }
//                } while(true)
//            }
//            return result
//        },
//        SlowEntityListField::class to fun(field, serial, cursor, report):Any?{
//            val result = arrayListOf<Any>()
//            if(serial[cursor.v] == '@') cursor.v++
//            else{
//                val factory = (field as DataListField<*>).factory
//                do{
//                    result += KoreConverter.decodeEntity(serial, cursor, factory(), report) ?:return null
//                    if(cursor.v >= serial.length) return result
//                    when(serial[cursor.v++]){
//                        '|'->{}//next item
//                        '@'->if (serial[cursor.v-2] != '\\') return result
//                        else-> return report(Data.ERROR.decode_error,"invalid entityList token:${serial[cursor.v-1]} / cursor:${cursor.v-1}")
//                    }
//                } while(true)
//            }
//            return result
//        },
//        DataMapField::class to fun(field, serial, cursor, report):Any?{
//            val result:HashMap<String, Data> = hashMapOf()
//            if(serial[cursor.v] == '@') cursor.v++
//            else{
//                val factory = (field as DataMapField<*>).factory
//                var pin = cursor.v
//                do {
//                    when (serial[cursor.v++]) {
//                        '|'->{
//                            if (serial[cursor.v-2] != '\\') {
//                                val key = KoreConverter.decodeString(serial.substring(pin, cursor.v - 1))
//                                result[key] = KoreConverter.decodeEntity(serial, cursor, factory(), report) ?:return null
//                                when (serial[cursor.v++]) {
//                                    '|'->pin = cursor.v
//                                    '@'->return result
//                                    else->return report(Data.ERROR.decode_error,"invalid entityMap token:${serial[cursor.v-1]} / cursor:${cursor.v-1}")
//                                }
//                            }
//                        }
//                    }
//                } while(true)
//            }
//            return result
//        },
//        SlowEntityMapField::class to fun(field, serial, cursor, report):Any?{
//            val result:HashMap<String, Data> = hashMapOf()
//            if(serial[cursor.v] == '@') cursor.v++
//            else{
//                val factory = (field as DataMapField<*>).factory
//                var pin = cursor.v
//                do {
//                    when (serial[cursor.v++]) {
//                        '|'->{
//                            if (serial[cursor.v-2] != '\\') {
//                                val key = KoreConverter.decodeString(serial.substring(pin, cursor.v - 1))
//                                result[key] = KoreConverter.decodeEntity(serial, cursor, factory(), report) ?:return null
//                                when (serial[cursor.v++]) {
//                                    '|'->pin = cursor.v
//                                    '@'->return result
//                                    else->return report(Data.ERROR.decode_error,"invalid entityMap token:${serial[cursor.v-1]} / cursor:${cursor.v-1}")
//                                }
//                            }
//                        }
//                    }
//                } while(true)
//            }
//            return result
//        }
    )
}