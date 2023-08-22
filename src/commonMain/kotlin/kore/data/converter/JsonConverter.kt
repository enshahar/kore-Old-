package kore.data.converter

import ein2b.core.date.eUtc
import ein2b.core.entity.*
import ein2b.core.entity.field.*
import ein2b.core.entity.indexer.Indexer
import kore.data.task.TaskStore
import kore.data.Data
import kore.data.Union
import kore.data.eSlowEntity
import kore.data.field.*
import kore.error.E
import kotlin.reflect.KClass

fun Data.serializeJson(block:((E) -> Unit)? = null):String? = JsonConverter.serialize(this, block)
fun <T: Data> T.unserializeJson(serial:String, block:((E) -> Unit)? = null):T? =
    JsonConverter.unserialize(this, serial, block)
fun eSlowEntity.serializeJson(block:((E) -> Unit)? = null):String? = JsonSerializer.serialize(this, block)
fun <T: eSlowEntity> T.unserializeJson(serial:String, block:((E) -> Unit)? = null):T? =
    JsonConverter.unserialize(this, serial, block)

@Suppress("NOTHING_TO_INLINE")
object JsonConverter: Converter<String> {
    override fun serialize(data: Data, block:((E)->Unit)?):String?{
        val report = Report()
        val result = encodeEntity(data, report)
        block?.also{ if(report.id != null) report.report{ err-> it(err) } }
        return result
    }
    override fun <ENTITY: Data> unserialize(data:ENTITY, value:String, block:((E)->Unit)?):ENTITY?{
        val report = Report()
        val result = decodeEntity(value, Cursor(0), data, report)
        block?.also{ if(report.id != null) report.report{ err-> it(err) } }
        return result
    }

    class Cursor(var v:Int)
    private inline fun wrapString(str:String) = "\"" + str + "\""
    fun encodeWrapString(str:String):String = wrapString(str)
    private val encodeValue:(String, Any, Field<*>, Report)->String={ name, v, _, _-> wrapString(name) + ":" + v.toString() }
    private val encodeStringValue:(String, Any, Field<*>, Report)->String={ name, v, _, _->
        wrapString(name) + ":" + wrapString(encodeString(v))
    }
    private val encodeValueList:(String, Any, Field<*>, Report)->String={ name, v, _, _-> wrapString(name) + ":[" + (v as List<*>).joinToString(","){"$it"} + "]" }
    private val encodeStringList:(String, Any, Field<*>, Report)->String={ name, v, _, _-> wrapString(name) +":[" + (v as List<*>).joinToString(","){ wrapString(
        encodeString(it)
    ) } + "]"}
    private val encodeValueMap:(String, Any, Field<*>, Report)->String={ name, v, _, _->
        var result = ""
        (v as Map<String,*>).forEach{(k,it)->result += ""","${encodeString(k)}":${it}"""}
        """"$name":{${if(result.isNotBlank()) result.substring(1) else ""}}"""
    }
    private val encodeStringMap:(String, Any, Field<*>, Report)->String={ name, v, _, _->
        var result = ""
        (v as Map<String,*>).forEach{(k,it)->result += ""","${encodeString(k)}":"${encodeString(it)}""""}
        """"$name":{${if(result.isNotBlank()) result.substring(1) else ""}}"""
    }
    private val encoders:HashMap<KClass<*>,(String, Any, Field<*>, Report)->String?> = hashMapOf(
        IntField::class to encodeValue,
        ShortField::class to encodeValue,
        LongField::class to encodeValue,
        UIntField::class to encodeValue,
        UShortField::class to encodeValue,
        ULongField::class to encodeValue,
        FloatField::class to encodeValue,
        DoubleField::class to encodeValue,
        BooleanField::class to encodeValue,
        EnumField::class to encodeStringValue,
        StringField::class to encodeStringValue,
        UtcField::class to { name, v, _, r->
            (v as? eUtc)?.let{
                encodeWrapString(name) + ":" + encodeWrapString(it.toString())
            }
        },
        IntListField::class to encodeValueList,
        ShortListField::class to encodeValueList,
        LongListField::class to encodeValueList,
        UIntListField::class to encodeValueList,
        UShortListField::class to encodeValueList,
        ULongListField::class to encodeValueList,
        FloatListField::class to encodeValueList,
        DoubleListField::class to encodeValueList,
        BooleanListField::class to encodeValueList,
        EnumListField::class to encodeStringList,
        StringListField::class to encodeStringList,
        IntMapField::class to encodeValueMap,
        ShortMapField::class to encodeValueMap,
        LongMapField::class to encodeValueMap,
        UIntMapField::class to encodeValueMap,
        UShortMapField::class to encodeValueMap,
        ULongMapField::class to encodeValueMap,
        FloatMapField::class to encodeValueMap,
        DoubleMapField::class to encodeValueMap,
        BooleanMapField::class to encodeValueMap,
        EnumMapField::class to encodeStringMap,
        StringMapField::class to encodeStringMap,
        SlowEntityField::class to { name, v, _, r->
            val result = encodeEntity(v,r)
            if(result != null) "\"" + name + "\":" + result else null },
        DataField::class to { name, v, _, r->
            val result = encodeEntity(v,r)
            if(result != null) "\"" + name + "\":" + result else null },
        DataListField::class to { name, v, _, r->
            var result = ""
            var isFirst = true
            if ((v as List<*>).all{ e ->
                encodeEntity(e!!, r)?.let{
                    result += if (isFirst){
                        isFirst = false
                        it
                    } else ",$it"
                } != null
            }) "\"" + name + "\":[" + result + "]" else null
        },
        SlowEntityListField::class to { name, v, _, r->
            var result = ""
            var isFirst = true
            if ((v as List<*>).all{ e ->
                    encodeEntity(e!!, r)?.let{
                        result += if (isFirst){
                            isFirst = false
                            it
                        } else ",$it"
                    } != null
                }) "\"" + name + "\":[" + result + "]" else null
        },
        DataMapField::class to { name, v, _, r->
            var result = ""
            var isFirst = true
            if ((v as Map<String, *>).all{ (k, v) ->
                encodeEntity(v!!, r)?.let{ value ->
                    result += if(isFirst){
                        isFirst = false
                        "\"" + encodeString(k) + "\":" + value
                    } else ",\"" + encodeString(k) + "\":" + value
                } != null
            }) "\"" + name + "\":{" + result + "}" else null
        },
        SlowEntityMapField::class to { name, v, _, r->
            var result = ""
            var isFirst = true
            if ((v as Map<String, *>).all{ (k, v) ->
                    encodeEntity(v!!, r)?.let{ value ->
                        result += if(isFirst){
                            isFirst = false
                            "\"" + encodeString(k) + "\":" + value
                        } else ",\"" + encodeString(k) + "\":" + value
                    } != null
                }) "\"" + name + "\":{" + result + "}" else null
        },
        UnionField::class to { name, v, f, r->
            val result = encodeUnionEntityM42(v,(f as UnionField<*>).union,r)
            if(result != null) "\"" + name + "\":" + result else null},
        UnionListField::class to { name, v, f, r->
            val un: Union<Data> = (f as UnionListField<*>).union
            var result = ""
            var isFirst = true
            if((v as List<*>).all{e ->
                encodeUnionEntityM42(e!!,un,r)?.let{
                    result += if (isFirst){
                        isFirst = false
                        it
                    } else ",$it"
                } != null
            }) "\"" + name + "\":[" + result + "]" else null
        },
        UnionMapField::class to { name, v, f, r->
            var result = ""
            var isFirst = true
            val un: Union<Data> = (f as UnionMapField<*>).union
            if((v as Map<String,*>).all{ (k, v) ->
                encodeUnionEntityM42(v!!,un,r)?.let{ value ->
                    result += if(isFirst){
                        isFirst = false
                        "\"" + encodeString(k) + "\":" + value
                    } else ",\"" + encodeString(k) + "\":" + value
                } != null
            }) "\"" + name + "\":{" + result + "}" else null
        }
    )
    private val decoders:HashMap<KClass<*>,(Field<*>, String, Cursor, Report)->Any?> = hashMapOf(
        IntField::class to { f, s, c, r-> decodeValue(s,c,r,String::toIntOrNull,f) },
        ShortField::class to { f, s, c, r-> decodeValue(s,c,r,String::toShortOrNull,f) },
        LongField::class to { f, s, c, r-> decodeValue(s,c,r,String::toLongOrNull,f) },
        UIntField::class to { f, s, c, r-> decodeValue(s,c,r,String::toUIntOrNull,f) },
        UShortField::class to { f, s, c, r-> decodeValue(s,c,r,String::toUShortOrNull,f) },
        ULongField::class to { f, s, c, r-> decodeValue(s,c,r,String::toULongOrNull,f) },
        FloatField::class to { f, s, c, r-> decodeValue(s,c,r,String::toFloatOrNull,f) },
        DoubleField::class to { f, s, c, r-> decodeValue(s,c,r,String::toDoubleOrNull,f) },
        BooleanField::class to { f, s, c, r-> decodeValue(s,c,r,String::toBooleanStrictOrNull,f) },
        StringField::class to { _, s, c, r-> decodeStringValue(s,c,r) },
        UtcField::class to fun(_: Field<*>, serial:String, cursor: Cursor, report:Report):Any?{
            return decodeStringValue(serial, cursor, report)?.let{
                eUtc.of(it) ?: report(
                    Data.ERROR.decode_error,
                    "invalid type eUtc,cursor:${cursor.v - 1},serial:$serial"
                )
            } ?: report(
                Data.ERROR.decode_error,
                "invalid eUtc,cursor:${cursor.v - 1},serial:$serial"
            )
        },
        EnumField::class to fun(field: Field<*>, serial:String, cursor: Cursor, report:Report):Any?{
            val value = decodeStringValue(serial,cursor,report) ?: return null
            return (field as EnumField<*>).enums.find{it.name == value } ?: report(Data.ERROR.decode_error,"invalid enum,cursor:${cursor.v-1},serial:$serial")
        },
        IntListField::class to { _, s, c, r-> decodeList(s,c,r,String::toIntOrNull) },
        ShortListField::class to { _, s, c, r-> decodeList(s,c,r,String::toShortOrNull) },
        LongListField::class to { _, s, c, r-> decodeList(s,c,r,String::toLongOrNull) },
        UIntListField::class to { _, s, c, r-> decodeList(s,c,r,String::toUIntOrNull) },
        UShortListField::class to { _, s, c, r-> decodeList(s,c,r,String::toUShortOrNull) },
        ULongListField::class to { _, s, c, r-> decodeList(s,c,r,String::toULongOrNull) },
        FloatListField::class to { _, s, c, r-> decodeList(s,c,r,String::toFloatOrNull) },
        DoubleListField::class to { _, s, c, r-> decodeList(s,c,r,String::toDoubleOrNull) },
        BooleanListField::class to { _, s, c, r-> decodeList(s,c,r,String::toBooleanStrictOrNull) },
        StringListField::class to { _, s, c, r-> decodeListPart(s,c,r) },
        EnumListField::class to fun(field: Field<*>, serial:String, cursor: Cursor, report:Report):Any?{
            val enums = (field as EnumListField<*>).enums
            val list = arrayListOf<Any>()
            openList(serial, cursor)
            if(skipSep(']', serial, cursor)) skipComma(serial, cursor)
            else{
                do{
                    val value = decodeStringValue(serial, cursor, report)
                    list += enums.find{ it.name == value } ?:return report(Data.ERROR.decode_error, "invalid enum,cursor:${cursor.v - 1},serial:$serial")
                    if(skipSep(']', serial, cursor)) break
                    skipComma(serial, cursor)
                }while(true)
            }
            return list
        },
        IntMapField::class to { f, s, c, r-> decodeMap(s,c,r,String::toIntOrNull,f) },
        ShortMapField::class to { f, s, c, r-> decodeMap(s,c,r,String::toShortOrNull,f) },
        LongMapField::class to { f, s, c, r-> decodeMap(s,c,r,String::toLongOrNull,f) },
        UIntMapField::class to { f, s, c, r-> decodeMap(s,c,r,String::toUIntOrNull,f) },
        UShortMapField::class to { f, s, c, r-> decodeMap(s,c,r,String::toUShortOrNull,f) },
        ULongMapField::class to { f, s, c, r-> decodeMap(s,c,r,String::toULongOrNull,f) },
        FloatMapField::class to { f, s, c, r-> decodeMap(s,c,r,String::toFloatOrNull,f) },
        DoubleMapField::class to { f, s, c, r-> decodeMap(s,c,r,String::toDoubleOrNull,f) },
        BooleanMapField::class to { f, s, c, r-> decodeMap(s,c,r,String::toBooleanStrictOrNull,f) },
        StringMapField::class to fun(_, serial:String, cursor: Cursor, report:Report):Any?{
            var key:String
            var value:String
            val result = hashMapOf<String,String>()
            openObject(serial,cursor)
            if(skipSep('}', serial, cursor)) skipComma(serial,cursor)
            else{
                while(skipNotSep('}', serial, cursor)){
                    key = decodeStringValue(serial,cursor,report) ?: return null
                    cursor.v++
                    value = decodeStringValue(serial,cursor,report) ?: return null
                    result[key] = value
                    if(skipSep('}', serial, cursor)) break
                    skipComma(serial,cursor)
                }
            }
            return result
        },
        EnumMapField::class to fun(field: Field<*>, serial:String, cursor: Cursor, report:Report):Any?{
            var key:String
            var value:String
            val result = hashMapOf<String,Any>()
            openObject(serial,cursor)
            if(skipSep('}', serial, cursor)) skipComma(serial,cursor)
            else{
                while(skipNotSep('}', serial, cursor)){
                    key = decodeStringValue(serial,cursor,report) ?: return null
                    cursor.v++
                    value = decodeStringValue(serial,cursor,report) ?: return null
                    result[key] = (field as EnumMapField<*>).enums.find{it.name == value } ?: return report(Data.ERROR.decode_error,"invalid enum,cursor:${cursor.v-1},serial:$serial")
                    if(skipSep('}', serial, cursor)) break
                    skipComma(serial,cursor)
                }
            }
            return result
        },
        DataField::class to { field, serial, cursor, report->
            decodeEntity(serial,cursor,(field as DataField<*>).factory(),report)
        },
        SlowEntityField::class to { field, serial, cursor, report->
            decodeEntity(serial,cursor,(field as DataField<*>).factory(),report)
        },
        DataListField::class to fun(field: Field<*>, serial:String, cursor: Cursor, report:Report):Any{
            val result = arrayListOf<Any>()
            openList(serial,cursor)
            if(skipSep(']', serial, cursor)) skipComma(serial,cursor)
            else{
                do{
                    if(decodeEntity(serial,cursor,(field as DataListField<*>).factory(),report)?.let{ result += it } == null) return report
                    if(skipSep(']', serial, cursor)) break
                    skipComma(serial,cursor)
                }while(true)
            }
            return result
        },
        SlowEntityListField::class to fun(field: Field<*>, serial:String, cursor: Cursor, report:Report):Any{
            val result = arrayListOf<Any>()
            openList(serial,cursor)
            if(skipSep(']', serial, cursor)) skipComma(serial,cursor)
            else{
                do{
                    if(decodeEntity(serial,cursor,(field as DataListField<*>).factory(),report)?.let{ result += it } == null) return report
                    if(skipSep(']', serial, cursor)) break
                    skipComma(serial,cursor)
                }while(true)
            }
            return result
        },
        SlowEntityMapField::class to { field, serial, cursor, report->
            val factory = (field as DataMapField<*>).factory
            var key:String?
            var value: Data?
            val result:HashMap<String, Data> = hashMapOf()
            openObject(serial,cursor)
            if(skipSep('}', serial, cursor)) skipComma(serial,cursor)
            else{
                while(skipNotSep('}', serial, cursor)){
                    key = decodeStringValue(serial,cursor,report)
                    cursor.v++
                    value = decodeEntity(serial,cursor,factory(),report)
                    result[key!!] = value!!
                    if(skipSep('}', serial, cursor)) break
                    skipComma(serial,cursor)
                }
            }
            result
        },
        DataMapField::class to { field, serial, cursor, report->
            val factory = (field as DataMapField<*>).factory
            var key:String?
            var value: Data?
            val result:HashMap<String, Data> = hashMapOf()
            openObject(serial,cursor)
            if(skipSep('}', serial, cursor)) skipComma(serial,cursor)
            else{
                while(skipNotSep('}', serial, cursor)){
                    key = decodeStringValue(serial,cursor,report)
                    cursor.v++
                    value = decodeEntity(serial,cursor,factory(),report)
                    result[key!!] = value!!
                    if(skipSep('}', serial, cursor)) break
                    skipComma(serial,cursor)
                }
            }
            result
        },
        UnionField::class to { field, serial, cursor, report->
            decodeUnionEntity(serial,cursor,(field as UnionField<*>).union,report)
        },
        UnionListField::class to fun(field: Field<*>, serial:String, cursor: Cursor, report:Report):Any?{
            val result = arrayListOf<Any>()
            openList(serial,cursor)
            if(skipSep(']', serial, cursor)) skipComma(serial,cursor)
            else{
                do{
                    result += decodeUnionEntity(serial,cursor,(field as UnionListField<*>).union,report) ?: return null
                    if(skipSep(']', serial, cursor)) break
                    skipComma(serial,cursor)
                }while(true)
            }
            return result
        },
        UnionMapField::class to fun(field: Field<*>, serial:String, cursor: Cursor, report:Report):Any?{
            var key:String
            var value: Data
            val result:HashMap<String, Data> = hashMapOf()
            openObject(serial,cursor)
            if(skipSep('}', serial, cursor)) skipComma(serial,cursor)
            else{
                while(skipNotSep('}', serial, cursor)){
                    key = decodeStringValue(serial,cursor,report) ?:return null
                    cursor.v++
                    value = decodeUnionEntity(serial,cursor,(field as UnionMapField<*>).union,report) ?:return null
                    result[key] = value
                    if(skipSep('}', serial, cursor)) break
                    skipComma(serial,cursor)
                }
            }
            return result
        }
    )
    fun setEncoder(type:KClass<*>,block:(name:String, v:Any, field: Field<*>, report:Report)->String?){
        encoders[type] = block
    }
    fun setDecoder(type:KClass<*>,block:(field: Field<*>, serial:String, cursor: Cursor, report:Report)->Any?){
        decoders[type] = block
    }
    /**
     * decodeStringValue 를 외부에 제공해주기 위한 함수
     */
    fun getDecodeStringValue(serial:String, cursor: Cursor, report:Report):String? = decodeStringValue(serial, cursor, report)

    /**
     * 문자열을 인코딩 디코딩할 때 이스케이프해야하는 특수문자 처리를 정의함
     * " :문자열 내부의 "는 이스케이핑한다
     * \n, \r :문자열의 개행은 이스케이핑한다
     */
    private inline fun encodeString(v:Any?):String = "$v".replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\\\r")
    private inline fun decodeString(v:String):String = v.replace("\\\"", "\"").replace("\\n", "\n").replace("\\r", "\r")

    /**
     * encoders 등록 안되어 있을때 기본 정책은 "k":"v" 로 인코딩 함
     */
    private fun encode(type:KClass<*>, k:String, v:Any, field: Field<*>, report:Report):String{
        return encoders[type]?.invoke(k,v,field,report) ?: encodeValue(k,v,field,report)
    }
//    private inline fun decode(field:Field<*>, serial:String, cursor:Cursor, report:Report):Any?{
//        return decoders[field::class]?.invoke(field,serial,cursor,report)
//    }
    /**
     * 단일 객체 내부의 프로퍼티들을 인코딩하고 리스트로 반환하는 함수
     * encodeEntity와 encodeUnionEntityM42에서 공통으로 사용하는 함수
     */
    private inline fun encodeObject(ent:Any, report:Report):List<String>?{
        val entity: Data = ent as Data
        val values:MutableMap<String,Any?> = entity._values ?:return arrayListOf()

        val type:KClass<out Data> = entity::class
        //val fields:HashMap<String, Field<*>> = Field[type] ?:return report(eEntity.ERROR.encode_error,"no fields $entity")
        val fields:HashMap<String, Field<*>> = entity.fields
        if (fields.size != values.size) return report(Data.ERROR.encode_error,"exist not initialized fields $entity")
        val result:ArrayList<String> = ArrayList<String>(fields.size).also{ list-> repeat(fields.size){ list.add("") } }

        values.forEach{(k,v)->
            val field: Field<*> = fields[k] ?:return report(Data.ERROR.encode_error,"no field ${type.simpleName}.${k}")
            //if(k == "data") log("values::${k}::${field::class}:$v")

            //1단계 키에 해당되는 store의 tasks를 가져와서
            val include = TaskStore.include(entity,k)
            val value = when{
                //include === Field.isOptional 일때 -> _values 에 값이 있으면 포함 없으면 포함 안함
                include === Field.isOptional-> v
                //include?.invoke() == false -> 포함 안함
                include?.invoke() == false-> null
                //그 외 -> _values 가 있으면 이걸로, 없으면 default 로, 둘 다 없으면 에러
                //else->v ?:Store.getDefault<Any>(entity,k)?.value ?:return report(eEntity.ERROR.encode_error,"not initialized encoding Entity ${type.simpleName}.${k}")
                else-> {
                    v ?: run {
                        entity._defaultMap?.get(
                            Indexer.get(entity::class, k)
                        )?.value ?:return report(Data.ERROR.encode_error,"not initialized encoding Entity ${type.simpleName}.${k}")
                    }
                }
            }
            if(value != null) Indexer.getOrNull(type,k)?.also{
                //if(k == "data") log("Indexer::${it}::${field::class.simpleName}:$v")
                result[it] = encode(field::class,k,value,field,report)
            }
        }
        return result.filter{ it.isNotBlank() }
    }
    private inline fun encodeEntity(entity:Any, report:Report):String? = encodeObject(entity,report)?.joinToString(",")?.let{ """{${it}}""" }

    private const val unionIndexKey:String = "@@@"
    private inline fun encodeUnionEntityM42(entity:Any, union: Union<*>, report:Report):String?{
        val unionType:KClass<out Any> = entity::class
        val index:Int = union.type.indexOf(unionType)
        if(index == -1) return report(Data.ERROR.encode_error,"invalid union subtype. unionType:${union.type},entity:${unionType.simpleName}")
        val result = encodeObject(entity,report)
        return if(result != null){
            """{"$unionIndexKey":${index}${if(result.isNotEmpty()) "," else ""}${result.joinToString(","){"${it.substring(0,1)}${it.substring(1)}"}}}"""
        }else null
        //return if(result != null) "{${result.joinToString(","){"${it.substring(0,1)}${it.substring(1)}"}}}" else null
    }
    private val SEP = " \t\n\r,]}".toCharArray()
    //private val SEP = ",]}".toCharArray()
    private inline fun<T> decodeValue(serial:String, cursor: Cursor, report:Report, block:String.()->T?, field: Field<*>? = null):T?{
        skipSpace(serial, cursor)
        val pin = cursor.v
        cursor.v = serial.indexOfAny(SEP,cursor.v++)
        if(cursor.v == -1) return report(Data.ERROR.decode_error,"invalid json form.field:$field,pin:${pin},serial:$serial")
        val chunk = serial.substring(pin,cursor.v)
        return chunk.block() ?:return report(Data.ERROR.decode_error,"invalid type.field:$field,chunk:*$chunk*,pin:${pin},cursor:${cursor.v},serial:$serial")
    }
    private inline fun decodeStringValue(serial:String, cursor: Cursor, report:Report):String?{
        if(skipNotSep('"', serial, cursor)) return report(Data.ERROR.decode_error,"invalid string form. cursor:${cursor.v},serial:$serial")
        cursor.v++
        val pin = cursor.v
        do{
            cursor.v = serial.indexOf('"', cursor.v++)
            if(cursor.v == -1){
                return report(Data.ERROR.decode_error,"invalid string form. pin:${pin},serial:$serial")
            }else{
                if(serial[cursor.v - 1] == '\\') cursor.v++
                else break
            }
        }while(true)
        return decodeString(serial.substring(pin,cursor.v++))
    }
    private inline fun <T> decodeList(serial:String, cursor: Cursor, report:Report, block:String.()->T?):Any?{
        openList(serial,cursor)
        val pin = cursor.v
        if(skipSep(']', serial, cursor)){
            skipComma(serial,cursor)
            return listOf<T>()
        }else{
            do{
                cursor.v = serial.indexOf(']', cursor.v++)
                if(cursor.v == -1){
                    return report(Data.ERROR.decode_error, "invalid list form. pin:${pin},cursor:${cursor.v},serial:$serial")
                }else{
                    if(serial[cursor.v - 1] == '\\') cursor.v++
                    else break
                }
            }while(true)
        }
        return serial.substring(pin,cursor.v++).trim().split(',').mapIndexed{ index,it ->
            it.trim().block() ?:return report(Data.ERROR.decode_error,"invalid type. $it,index:$index")
        }
    }
    private inline fun decodeListPart(serial:String, cursor: Cursor, report:Report):List<String>?{
        val list = arrayListOf<String>()
        openList(serial,cursor)
        if(skipSep(']', serial, cursor)) skipComma(serial,cursor)
        else{
            do{
                list += decodeStringValue(serial,cursor,report) ?:return null
                if(skipSep(']', serial, cursor)) break
                skipComma(serial,cursor)
            }while(true)
        }
        return list
    }
    private inline fun <T> decodeMap(serial:String, cursor: Cursor, report:Report, block:String.()->T?, field: Field<*>):Any?{
        var key:String
        var value:T
        val result = hashMapOf<String,T>()
        openObject(serial,cursor)
        if(skipSep('}', serial, cursor)) skipComma(serial,cursor)
        else{
            while(skipNotSep('}', serial, cursor)){
                key = decodeStringValue(serial,cursor,report) ?:return null
                cursor.v++
                value = decodeValue(serial,cursor,report,block,field) ?:return null
                result[key] = value
                if(skipSep('}', serial, cursor)) break
                skipComma(serial,cursor)
            }
        }
        return result
    }

    private val entry:Map.Entry<String, Field<*>> = object:Map.Entry<String, Field<*>>{
        override val key:String get() = throw E(Data.ERROR.encode_error,"")
        override val value: Field<*> get() = throw E(Data.ERROR.encode_error,"")
    }

    private inline fun openObject(serial:String,cursor: Cursor):Boolean{
        return if(skipSep('{', serial, cursor)) true
        else throw E(Data.ERROR.decode_error,"invalid object,cursor:${cursor.v},serial[cursor.v] = ${serial.substring(cursor.v)},serial:$serial")
    }
    private inline fun openList(serial:String,cursor: Cursor):Boolean{
        return if(skipSep('[', serial, cursor)) true
        else throw E(Data.ERROR.decode_error,"invalid list,cursor:${cursor.v},serial:$serial")
    }
    private inline fun key(serial:String, cursor: Cursor, report:Report):String?{
        val key = decodeStringValue(serial,cursor,report) ?:return null
        skipSpace(serial, cursor)
        if(serial[cursor.v++] != ':') throw E(Data.ERROR.decode_error,"invalid key form,key:${key},cursor:${cursor.v-1},serial:$serial")
        return key
    }
    private inline fun <ENTITY: Data> decodeEntity(serial:String, cursor: Cursor, entity:ENTITY, report:Report):ENTITY?{
        val type:KClass<out Data> = entity::class
        //val fields:HashMap<String, Field<*>> = Field[type] ?: return entity//return report(eEntity.ERROR.decode_error,"no fields:${type.simpleName}")
        val fields:HashMap<String, Field<*>> = entity.fields
        val convert:ArrayList<Map.Entry<String, Field<*>>> = ArrayList<Map.Entry<String, Field<*>>>(fields.size).also{ list-> repeat(fields.size){ list.add(
            entry
        ) } }
        fields.forEach{
            convert[Indexer.get(type,it.key)] = it
        }
        openObject(serial,cursor)
        while(skipNotSep('}', serial, cursor)){
            val key = key(serial, cursor, report) ?: return entity
            if(Indexer.getOrNull(type,key) == null){
                try{
                    passValue(key, serial, cursor, report)
                }catch(e: E){
                    return report(e.id, e.message, *e.result)
                }
            }else{
                val field = convert[Indexer.get(type,key)]
                val v = decoders[field.value::class]?.invoke(field.value,serial,cursor,report) ?:return report(Data.ERROR.decode_error,"no value:${type.simpleName}:${key}")
                try{
                    entity.setRawValue(field.key, v)
                }catch(e: E){
                    return report(e.id, e.message, *e.result)
                }
            }
            //종료 처리
            if(skipSep('}', serial, cursor)) break
            skipComma(serial,cursor)
        }
        return entity
    }
    private inline fun <ENTITY: Data,T: Union<ENTITY>> decodeUnionEntity(serial:String, cursor: Cursor, union:T, report:Report):ENTITY?{
        openObject(serial,cursor)

        var isM42Json = false
        var entity:ENTITY? = null
        val pin = cursor.v
        val unionKey = key(serial,cursor,report) ?:return null
        var unionIndex:Int? = null
        if(unionKey == unionIndexKey){
            isM42Json = true
            unionIndex = decoders[IntField::class]?.invoke(IntField,serial,cursor,report)?.let{ "$it".toIntOrNull() }
            if(unionIndex == null) isM42Json = false
            else skipComma(serial,cursor)
        }

        if(isM42Json){
            var isError = false
            entity = union.factories[unionIndex!!]()
            val type:KClass<out Data> = entity::class
            val value:MutableMap<String,Any?> = entity._values ?:let{
                skipSep('}', serial, cursor)
                return entity
            }// ?:throw Error(eEntity.ERROR.decode_error,"no value:${type}:$entity")
            //val fields:HashMap<String, Field<*>> = Field[type] ?: throw Error(eEntity.ERROR.decode_error,"no fields $entity")
            val fields:HashMap<String, Field<*>> = entity.fields
            val convert:ArrayList<Map.Entry<String, Field<*>>> = ArrayList<Map.Entry<String, Field<*>>>(fields.size).also{ list->repeat(fields.size){list.add(
                entry
            )}}
            fields.forEach{
                if(Indexer.getOrNull(type,it.key) == null){
                    isError = true
                    return@forEach
                }
                convert[Indexer.get(type,it.key)] = it
            }
            if(!isError){
                //openObject(serial,cursor)
                while(skipNotSep('}', serial, cursor)){
                    val key = key(serial,cursor,report)?.split('|')?.last() ?: return null
                    val field = convert[Indexer.get(type,key)]
                    value[field.key] = decoders[field.value::class]?.invoke(field.value,serial,cursor,report)
                    if(skipSep('}', serial, cursor)) break
                    skipComma(serial,cursor)
                }
                return entity
            }
        }

        union.factories.forEach{ factory ->
            val factoryEntity = factory()
            cursor.v = pin
            var isError = false
            val type:KClass<out Data> = factoryEntity::class
            val value:MutableMap<String,Any?> = factoryEntity._values ?:return@forEach
            //val fields:HashMap<String, Field<*>> = Field[type] ?:return@forEach
            val fields:HashMap<String, Field<*>> = factoryEntity.fields
            val convert:ArrayList<Map.Entry<String, Field<*>>> = ArrayList<Map.Entry<String, Field<*>>>(fields.size).also{ list->repeat(fields.size){ list.add(
                entry
            ) }}
            fields.forEach{
                if(Indexer.getOrNull(type,it.key) == null){
                    isError = true
                    @Suppress("LABEL_NAME_CLASH")
                    return@forEach
                }
                convert[Indexer.get(type,it.key)] = it
            }
            if(isError) return@forEach

            while(skipNotSep('}', serial, cursor)){
                val key = key(serial,cursor,report) ?: return@forEach
                val idx = Indexer.getOrNull(type,key)
                if(idx == null){
                    isError = true
                    break
                }
                val field = convert[idx]
                value[field.key] = decoders[field.value::class]?.invoke(field.value,serial,cursor,report)
                if(skipSep('}', serial, cursor)) break
                skipComma(serial,cursor)
            }
            if(isError) return@forEach
            return factoryEntity
        }

        return entity?:throw E(Data.ERROR.decode_error,"Union Decode Error")
    }

    private inline fun skipSpace(serial:String, cursor: Cursor){
        var isChanged = false
        var i = cursor.v
        var limit = 200
        do{
            val c = serial[i++]
            if(c == ' ' || c == '\t' || c == '\n' || c == '\r'){
                isChanged = true
            } else break
        }while(limit-- > 0)
        if(isChanged) cursor.v = i-1
    }
    private inline fun skipSep(sep:Char, serial:String, cursor: Cursor):Boolean{
        skipSpace(serial, cursor)
        return if(serial[cursor.v] == sep){
            cursor.v++
            true
        }else false
    }
    private inline fun skipNotSep(sep:Char, serial:String, cursor: Cursor):Boolean{
        skipSpace(serial, cursor)
        return serial[cursor.v] != sep
    }
    private inline fun skipComma(serial:String,cursor: Cursor){
        skipSpace(serial, cursor)
        if(serial.length >= cursor.v && serial[cursor.v] == ','){
            cursor.v++
        }
    }
    private fun passValue(key:String, serial:String, cursor: Cursor, report:Report){
        skipSpace(serial, cursor)
        when(val curr = serial[cursor.v]){
            '['->{
                openList(serial, cursor)
                if(skipSep(']', serial, cursor)) skipComma(serial, cursor)
                else{
                    var idx = -1
                    do{
                        idx++
                        passValue("$key-$idx", serial, cursor, report)
                        if(skipSep(']', serial, cursor)){
                            cursor.v++
                            break
                        }
                        skipComma(serial, cursor)
                    }while(true)
                }
            }
            '{'->{
                openObject(serial, cursor)
                if(skipSep('}', serial, cursor)) skipComma(serial, cursor)
                else{
                    while(skipNotSep('}', serial, cursor)){
                        val mapKey = decodeStringValue(serial, cursor, report)
                        if(mapKey == null){
                            report<Map<String,*>>(Data.ERROR.decode_error,"no passValue|$key map key null")
                            break
                        }
                        cursor.v++
                        passValue(mapKey, serial, cursor, report)
                        if(skipSep('}', serial, cursor)) break
                        skipComma(serial,cursor)
                    }
                }
            }
            'n'-> cursor.v += 4 //null 만큼 전진
            't'-> cursor.v += 4 //true 만큼 전진
            'f'-> cursor.v += 5 //false 만큼 전진
            '"'-> decodeStringValue(serial, cursor, report) //문자열 및 이스케이프 확인하면서 전진
            else->{
                //종료 문자열 " \t\n\r,]}" 위치까지 전진
                if("0123456789-.".indexOf(curr) != -1) cursor.v = serial.indexOfAny(SEP, cursor.v++)
                else report(Data.ERROR.decode_error,"no passValue|$key|$curr")
            }
        }
    }
}