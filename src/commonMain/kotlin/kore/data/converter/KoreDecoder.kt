package kore.data.converter

import kore.data.Data
import kore.data.field.*
import kore.wrap.Wrap
import kotlin.reflect.KClass

internal object KoreDecoder{
    fun <DATA> decode(data: DATA, value: String): Wrap<DATA> {
        TODO("Not yet implemented")
    }

    internal val decoders:HashMap<KClass<*>,(Field<*>, String, KoreConverter.Cursor)-> Wrap<Any>> = hashMapOf(
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