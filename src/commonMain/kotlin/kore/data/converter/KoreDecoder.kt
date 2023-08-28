@file:Suppress("NOTHING_TO_INLINE")

package kore.data.converter

import kore.data.Data
import kore.data.SlowData
import kore.data.converter.KoreConverter.Cursor
import kore.data.converter.KoreConverter.Cursor.DecodeNoListTeminator
import kore.data.converter.KoreConverter.OPTIONAL_NULL_C
import kore.data.converter.KoreConverter.STRINGLIST_EMPTY_C
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
    internal fun <DATA: Data> data(cursor:Cursor, data:DATA):Wrap<DATA>{
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
    private inline fun <VALUE:Any> valueMap(cursor:Cursor, field: Field<*>, crossinline block:String.()->VALUE?):Wrap<HashMap<String, VALUE>>{
        return stringList(cursor).map{
            var key:String? = null
            it.fold(hashMapOf()) {acc, item->
                if(key == null) key = item
                else{
                    acc[key!!] = item.block() ?: throw DecodeInvalidMapValue(field, item, key!!)
                    key = null
                }
                acc
            }
        }
    }
    private inline fun stringValue(cursor: Cursor):Wrap<String>{
        val encoded = cursor.encoded
        val pin = cursor.v
        var at = pin
        do {
            at = encoded.indexOf('|', at)
            if (at == -1) { /** encoded 맨 끝까지 문자열인 경우 */
                cursor.v = encoded.length
                return W(decodeString(encoded.substring(pin, cursor.v)))
            } else if(at == pin) return W("") /** 길이 0인 문자열  || 인 상황 */
            else if (encoded[at-1] == '\\') at++ /** \| 스킵하기 */
            else {
                cursor.v = at
                return W(decodeString(encoded.substring(pin, at)))
            }
        } while(true)
    }
    private val regStringSplit = """(?<!\\)\|""".toRegex()
    private inline fun stringList(cursor: Cursor):Wrap<List<String>>{
        val list:ArrayList<String> = arrayListOf()
        if(cursor.curr == STRINGLIST_EMPTY_C) { /** !로 마크된 빈 리스트 */
            cursor.v++
            return W(list)
        }
        val encoded = cursor.encoded
        val pin = cursor.v
        var at = pin
        do{
            at = encoded.indexOf('@', at)
            if(at == -1) return W(DecodeNoListTeminator(encoded.substring(pin)))
            else if(encoded[at - 1] == '\\') at++
            else break
        }while(true)
        cursor.v = at
        return W(encoded.substring(pin, at).splitToSequence(regStringSplit).map{decodeString(it)}.toList())
    }
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
        StringField::class to {c, _->stringValue(c) },
        StringListField::class to {c, _-> stringList(c)},
        StringMapField::class to {c, _->
            stringList(c).map{
                var key:String? = null
                it.fold(hashMapOf<String, String>()) {acc, item->
                    if(key == null) key = item
                    else{
                        acc[key!!] = item
                        key = null
                    }
                    acc
                }
            }
        },
        EnumField::class to {c,f->
            value(c, f){toIntOrNull()}.map{(f as EnumField<*>).enums[it]}
        },
        EnumListField::class to {c, f->
            val enums = (f as EnumListField<*>).enums
            valueList(c, f){toIntOrNull()}.map{it.map{item->enums[item]}}
        },
        EnumMapField::class to {c, f->
            val enums = (f as EnumMapField<*>).enums
            stringList(c).map{
                var key:String? = null
                it.fold(hashMapOf<String, Enum<*>>()) {acc, item->
                    if(key == null) key = item
                    else{
                        acc[key!!] = enums[item.toInt()]
                        key = null
                    }
                    acc
                }
            }
        },
        DataField::class to { c, f->data(c, (f as DataField<*>).factory())},
        DataListField::class to {c, f->
            val result: ArrayList<Any> = arrayListOf()
            var error:Throwable? = null
            if(c.curr == '@') c.v++ /** 빈리스트*/
            else{
                val factory: () -> Data = (f as DataListField<*>).factory
                val encoded:String = c.encoded
                do{
                    result.add(data(c, factory()))
                    if(c.v < encoded.length) {
                        when(c.getAndNext()) {
                            '|' -> {} /** 다음데이터 */
                            '@' -> break /** 리스트끝 */
                            else -> { /** 잘못된 토큰 */
                                error = DecodeNoListTeminator(encoded.substring(c.v - 1))
                                break
                            }
                        }
                    }else {
                        error = DecodeNoListTeminator(encoded.substring(c.v - 1))
                        break
                    }
                } while(true)
            }
            if(error == null) W(result) else W(error)
        },
        DataMapField::class to {c, f->
            val result:HashMap<String, Data> = hashMapOf()
            var error:Throwable? = null
            if(c.curr == '@') c.v++
            else{
                val factory: () -> Data = (f as DataMapField<*>).factory
                var pin = c.v
                val encoded = c.encoded
                do {
                    var key:String? = null
                    stringValue(c).get{key = it} orFail {error = it}
                    if(error != null) break
                    c.v++
                    data(c, factory()).get{result[key!!] = it} orFail {error = it}
                    if(error != null) break
                    if(c.v < encoded.length) {
                        when (c.getAndNext()) {
                            '|' -> c.v++ /** 다음데이터 */
                            '@' -> break /** 리스트끝 */
                            else -> {
                                error = DecodeNoListTeminator(encoded.substring(c.v - 1))
                                break
                            }
                        }
                    }else{
                        error = DecodeNoListTeminator(encoded.substring(c.v - 1))
                        break
                    }
                } while(true)
            }
            if(error == null) W(result) else W(error!!)
        },
        UnionField::class to {c, f->
            var result:Wrap<*>? = null
            value(c, f){toIntOrNull()}.get{
                val factory:()->Data = (f as UnionField<*>).union.factories[it]
                c.v++
                result = data(c, factory())
            } orFail {
                result = W<Data>(it)
            }
            result!!
        },
        UnionListField::class to {c, f->
            val result: ArrayList<Any> = arrayListOf()
            var error:Throwable? = null
            if(c.curr == '@') c.v++ /** 빈리스트*/
            else{
                val factory: () -> Data = (f as DataListField<*>).factory
                val encoded:String = c.encoded
                do{
                    result.add(data(c, factory()))
                    if(c.v < encoded.length) {
                        when(c.getAndNext()) {
                            '|' -> {} /** 다음데이터 */
                            '@' -> break /** 리스트끝 */
                            else -> { /** 잘못된 토큰 */
                                error = DecodeNoListTeminator(encoded.substring(c.v - 1))
                                break
                            }
                        }
                    }else {
                        error = DecodeNoListTeminator(encoded.substring(c.v - 1))
                        break
                    }
                } while(true)
            }
            if(error == null) W(result) else W(error)
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

    )
}