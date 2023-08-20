package ein2b.core.entity.encoder

import ein2b.core.date.eUtc
import ein2b.core.entity.*
import ein2b.core.entity.field.*
import ein2b.core.entity.indexer.Indexer
import ein2b.core.entity.task.Store
import kore.data.Data
import kore.data.eSlowEntity
import kotlin.reflect.KClass

fun Data.serializeEin(block:((Error) -> Unit)? = null):String? = EinSerializer.serialize(this, block)
fun <T: Data> T.unserializeEin(serial:String, block:((Error) -> Unit)? = null):T? = EinSerializer.unserialize(this, serial, block)
fun eSlowEntity.serializeEin(block:((Error) -> Unit)? = null):String? = EinSerializer.serialize(this, block)
fun <T: eSlowEntity> T.unserializeEin(serial:String, block:((Error) -> Unit)? = null):T? = EinSerializer.unserialize(this, serial, block)

private val encodeValue:(Any, Field<*>, Report)->String = { v, _, _->"$v"}
private val encodeValueList:(Any, Field<*>, Report)->String = { v, _, _->(v as List<*>).joinToString("|")+"@"}

@Suppress("NOTHING_TO_INLINE")
object EinSerializer:Serializer<String>{
    private const val optionalNullValue:Char = '~'
    private const val emptyStringListValue = '!'

    class Cursor(var v:Int)
    private val encoders:HashMap<KClass<*>,(Any, Field<*>, Report)->String?> = hashMapOf(
        IntField::class to encodeValue,
        ShortField::class to encodeValue,
        LongField::class to encodeValue,
        UIntField::class to encodeValue,
        UShortField::class to encodeValue,
        ULongField::class to encodeValue,
        FloatField::class to encodeValue,
        DoubleField::class to encodeValue,
        BooleanField::class to encodeValue,
        IntListField::class to encodeValueList,
        ShortListField::class to encodeValueList,
        LongListField::class to encodeValueList,
        UIntListField::class to encodeValueList,
        UShortListField::class to encodeValueList,
        ULongListField::class to encodeValueList,
        FloatListField::class to encodeValueList,
        DoubleListField::class to encodeValueList,
        BooleanListField::class to encodeValueList,
        IntMapField::class to { v, _, _-> encodeValueMap(v) },
        ShortMapField::class to { v, _, _-> encodeValueMap(v) },
        LongMapField::class to { v, _, _-> encodeValueMap(v) },
        UIntMapField::class to { v, _, _-> encodeValueMap(v) },
        UShortMapField::class to { v, _, _-> encodeValueMap(v) },
        ULongMapField::class to { v, _, _-> encodeValueMap(v) },
        FloatMapField::class to { v, _, _-> encodeValueMap(v) },
        DoubleMapField::class to { v, _, _-> encodeValueMap(v) },
        BooleanMapField::class to { v, _, _-> encodeValueMap(v) },

        UtcField::class to { v, _, _-> (v as? eUtc)?.let{ encodeString(it.toString()) } },
        StringField::class to { v, _, _-> encodeString(v) },
        StringListField::class to { v, _, _->(v as List<*>).let{
            if(it.isEmpty())
                "${emptyStringListValue}"
            else
                it.joinToString("|"){ encodeString(it) }+"@"}
        },
        StringMapField::class to { v, _, _->
            var result = ""
            (v as Map<String,*>).forEach{(k,it)->result += "|" + encodeString(k) + "|" + encodeString(it) }
            encodeResult(result)
        },
        EnumField::class to { v, field, r->
            val enums:Array<*> = (field as EnumField<*>).enums
            val index:Int = enums.indexOf(v)
            if(index != -1) "$index" else r.invoke(Data.ERROR.encode_error,"invalid enum. enums:${enums.joinToString(",")},enum:$v")
        },
        EnumListField::class to { v, field, r ->
            val enums: Array<*> = (field as EnumListField<*>).enums
            var result = ""
            if ((v as List<*>).all { e ->
                    val index = enums.indexOf(e)
                    if (index == -1) {
                        r.invoke(Data.ERROR.encode_error, "invalid enum. enums:${enums.joinToString(",")},enum:$e")
                    } else {
                        result += "|$index"
                    } != null
                }) encodeResult(result) else null
        },
        EnumMapField::class to { v, field, r->
            val enums:Array<*> = (field as EnumMapField<*>).enums
            var result = ""
            if ((v as Map<String,*>).all { (k,e)->
                    val index = enums.indexOf(e)
                    if (index == -1) {
                        r.invoke(Data.ERROR.encode_error, "invalid enum. enums:${enums.joinToString(",")},enum:$e")
                    } else {
                        result += "|" + encodeString(k) + "|" + index.toString()
                    } != null
                }) encodeResult(result) else null
        },
        EntityField::class to { v, _, r-> encodeEntity(v,r) },
        SlowEntityField::class to { v, _, r-> encodeEntity(v,r) },
        EntityListField::class to { v, _, r->
            var result = ""
            if((v as List<*>).all { e -> encodeEntity(e!!,r)?.let { result+="|$it" } != null}) encodeResult(result) else null
        },
        SlowEntityListField::class to { v, _, r->
            var result = ""
            if((v as List<*>).all { e -> encodeEntity(e!!,r)?.let { result+="|$it" } != null}) encodeResult(result) else null
        },
        EntityMapField::class to { v, _, r->
            var result = ""
            if ((v as Map<String, *>).all { (k, it) ->
                    encodeEntity(it!!, r)?.let { value ->
                        result += "|" + encodeString(k) + "|" + value
                    } != null
                }
            ) encodeResult(result) else null
        },
        SlowEntityMapField::class to { v, _, r->
            var result = ""
            if ((v as Map<String, *>).all { (k, it) ->
                    encodeEntity(it!!, r)?.let { value ->
                        result += "|" + encodeString(k) + "|" + value
                    } != null
                }
            ) encodeResult(result) else null
        },
        UnionField::class to { v, field, r-> encodeUnion(v,(field as UnionField<*>).union,r) },
        UnionListField::class to { v, field, r->
            val un: Union<Data> = (field as UnionListField<*>).union
            var result = ""
            if((v as List<*>).all{ e -> encodeUnion(e!!,un,r)?.let{ result += "|$it" } != null}) encodeResult(result) else null
        },
        UnionMapField::class to { v, field, r->
            var result = ""
            val un:Union<Data> = (field as UnionMapField<*>).union
            if((v as Map<String, *>).all{ (k, it) ->
                    encodeUnion(it!!, un, r)?.let{ value ->
                        result += "|" + encodeString(k) + "|" + value
                    } != null
                }
            ) encodeResult(result) else null
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
        IntListField::class to { _, s, c, r-> decodeValueList(s,c,r,String::toIntOrNull) },
        ShortListField::class to { _, s, c, r-> decodeValueList(s,c,r,String::toShortOrNull) },
        LongListField::class to { _, s, c, r-> decodeValueList(s,c,r,String::toLongOrNull) },
        UIntListField::class to { _, s, c, r-> decodeValueList(s,c,r,String::toUIntOrNull) },
        UShortListField::class to { _, s, c, r-> decodeValueList(s,c,r,String::toUShortOrNull) },
        ULongListField::class to { _, s, c, r-> decodeValueList(s,c,r,String::toULongOrNull) },
        FloatListField::class to { _, s, c, r-> decodeValueList(s,c,r,String::toFloatOrNull) },
        DoubleListField::class to { _, s, c, r-> decodeValueList(s,c,r,String::toDoubleOrNull) },
        BooleanListField::class to { _, s, c, r-> decodeValueList(s,c,r,String::toBooleanStrictOrNull) },
        IntMapField::class to { _, s, c, r-> decodeValueMap(s,c,r,String::toIntOrNull) },
        ShortMapField::class to { _, s, c, r-> decodeValueMap(s,c,r,String::toShortOrNull) },
        LongMapField::class to { _, s, c, r-> decodeValueMap(s,c,r,String::toLongOrNull) },
        UIntMapField::class to { _, s, c, r-> decodeValueMap(s,c,r,String::toUIntOrNull) },
        UShortMapField::class to { _, s, c, r-> decodeValueMap(s,c,r,String::toUShortOrNull) },
        ULongMapField::class to { _, s, c, r-> decodeValueMap(s,c,r,String::toULongOrNull) },
        FloatMapField::class to { _, s, c, r-> decodeValueMap(s,c,r,String::toFloatOrNull) },
        DoubleMapField::class to { _, s, c, r-> decodeValueMap(s,c,r,String::toDoubleOrNull) },
        BooleanMapField::class to { _, s, c, r-> decodeValueMap(s,c,r,String::toBooleanStrictOrNull) },

        UtcField::class to { _, serial, cursor, _-> decodeStringValue(serial, cursor).let{ eUtc.of(it) } },
        StringField::class to { _, serial, cursor, _-> decodeStringValue(serial, cursor) },
        StringListField::class to { _, s, c, r-> decodeStringList(s,c,r)?.map(this::decodeString)},
        StringMapField::class to fun(_, serial, cursor, report):Any?{
            var key:String? = null
            val result = hashMapOf<String,String>()
            decodeStringList(serial, cursor, report)?.forEach{
                if(key == null) key = decodeString(it)
                else{
                    result[key!!] = decodeString(it)
                    key = null
                }
            } ?: return null
            return result
        },
        EnumField::class to fun(field, serial, cursor, report):Any?{
            val idx = decodeValue(serial,cursor,report,String::toIntOrNull) ?:return null
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
            decodeStringList(serial,cursor,report)?.forEach {
                if(key == null) key = decodeString(it)
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
            decodeEntity( serial, cursor, (field as UnionField<*>).union.factories[serial.substring(pin,cursor.v++).toInt()](), report )
        },
        UnionListField::class to fun(field, serial, cursor, report):Any?{
            val result = arrayListOf<Any>()
            if(serial[cursor.v] == '@') cursor.v++
            else {
                val factories = (field as UnionListField<*>).union.factories
                do {
                    val pin = cursor.v
                    cursor.v = serial.indexOf('|',pin)
                    result += decodeEntity(serial,cursor,factories[serial.substring(pin,cursor.v++).toInt()](),report) ?: return null
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
                                val key = decodeString(serial.substring(keyPin,cursor.v-1))
                                val pin = cursor.v
                                cursor.v = serial.indexOf('|',pin)
                                result[key] = decodeEntity(serial,cursor,factories[serial.substring(pin,cursor.v++).toInt()](),report) ?: return null
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
        EntityField::class to { field, serial, cursor, report->
            if(serial[cursor.v] == '|') ""
            else decodeEntity(serial,cursor,(field as EntityField<*>).factory(),report)
        },
        SlowEntityField::class to { field, serial, cursor, report->
            if(serial[cursor.v] == '|') ""
            else decodeEntity(serial,cursor,(field as EntityField<*>).factory(),report)
        },
        EntityListField::class to fun(field, serial, cursor, report):Any?{
            val result = arrayListOf<Any>()
            if(serial[cursor.v] == '@') cursor.v++
            else{
                val factory = (field as EntityListField<*>).factory
                do{
                    result += decodeEntity(serial,cursor,factory(),report) ?:return null
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
                val factory = (field as EntityListField<*>).factory
                do{
                    result += decodeEntity(serial,cursor,factory(),report) ?:return null
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
        EntityMapField::class to fun(field, serial, cursor, report):Any?{
            val result:HashMap<String, Data> = hashMapOf()
            if(serial[cursor.v] == '@') cursor.v++
            else{
                val factory = (field as EntityMapField<*>).factory
                var pin = cursor.v
                do {
                    when (serial[cursor.v++]) {
                        '|'->{
                            if (serial[cursor.v-2] != '\\') {
                                val key = decodeString(serial.substring(pin,cursor.v-1))
                                result[key] = decodeEntity(serial,cursor,factory(),report) ?:return null
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
                val factory = (field as EntityMapField<*>).factory
                var pin = cursor.v
                do {
                    when (serial[cursor.v++]) {
                        '|'->{
                            if (serial[cursor.v-2] != '\\') {
                                val key = decodeString(serial.substring(pin,cursor.v-1))
                                result[key] = decodeEntity(serial,cursor,factory(),report) ?:return null
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
    fun setEncoder(type:KClass<*>,block:(Any, Field<*>, Report)->String){
        encoders[type] = block}
    fun setDecoder(type:KClass<*>,block:(field: Field<*>, serial:String, cursor: Cursor, report: Report)->Any?){
        decoders[type] = block}
    override fun serialize(entity: Data, block:((Error)->Unit)?):String? {
        val report = Report()
        val result = encodeEntity(entity, report)
        if(result == null){
            if(block != null) report.report(block)
        }
        return result
    }

    override fun <ENTITY: Data> unserialize(entity:ENTITY, value:String, block:((Error)->Unit)?):ENTITY? {
        val report = Report()
        val result = decodeEntity(value, Cursor(0), entity, report)
        if(result == null){
            if(block != null) report.report(block)
        }
        return result
    }

    /**
     * 문자열을 인코딩 디코딩할 때 이스케이프해야하는 특수문자 처리를 정의함
     * @ : 리스트와 맵의 종결자로 사용함
     * | : 모든 요소의 구분자로 사용함(리스트, 맵의 키와 값의 구분자, 엔티티 필드 구분자 등)
     * \n, \r : 문자열의 개행은 이스케이핑한다
     */
    private inline fun encodeString(v:Any?):String = "$v".replace("@","\\@").
        replace("|","\\|").replace("~", "\\~").
        replace("\n", "\\n").replace("\r", "\\\\r")
    private inline fun decodeString(v:String):String = v.replace("\\@","@").replace("\\|","|").replace("\\~","~").
        replace("\\n", "\n").replace("\\r", "\r")

    /**
     * decodeStringValue 를 외부에 제공해주기 위한 함수
     */
    fun getDecodeStringValue(serial:String, cursor:Cursor):String = decodeStringValue(serial, cursor)

    /**
     * 디코더에서 사용하는 빈 객체
     * get()으로 값을 가져올 수 없음
     */
    private val entry:Map.Entry<String,Field<*>> = object:Map.Entry<String,Field<*>>{
        override val key:String get() = throw Error(Data.ERROR.encode_error,"")
        override val value:Field<*> get() = throw Error(Data.ERROR.encode_error,"")
    }

    /**
     * Encode-------------------------------
     */
    private inline fun encodeEntity(ent:Any, report:Report):String?{
        val entity: Data = ent as Data

        val fields:HashMap<String, Field<*>> = entity.fields
        if(fields.isEmpty()) return "|"

        val type:KClass<out Data> = entity::class
//        log("----encodeEntity0--${type.simpleName}------------------")

        // 필드가 존재하므로, 여기서 values가 널이면 문제가 있음을 확신할 수 있다.
        val values:MutableMap<String,Any?> = entity._values ?: return report(Data.ERROR.encode_error,"no values $entity")
//        log("----encodeEntity1--${type.simpleName}------------------")
        //val fields:HashMap<String, Field<*>> = Field[type] ?: return report(eEntity.ERROR.encode_error,"no fields $entity")
        if(fields.size != values.size) return report(Data.ERROR.encode_error,"exist not initialized fields $entity")
        val result:ArrayList<String> = ArrayList<String>(fields.size).also{list->repeat(fields.size){list.add("")}}

        return if(values.all{ (k,v) ->
            val field:Field<*> = fields[k] ?: return report(Data.ERROR.encode_error,"no field ${type.simpleName}.${k}")

            //1단계 키에 해당되는 store의 tasks를 가져와서
            val include = Store.getInclude(entity,k)
            val value = when{
                //include === Field.isOptional 일때 -> _values 에 값이 있으면 포함 없으면 포함 안함
                include === Field.isOptional-> v ?: optionalNullValue
                //include?.invoke() == false -> 포함 안함
                include?.invoke() == false-> null
                //그 외 -> _values 가 있으면 이걸로, 없으면 default 로, 둘 다 없으면 에러
                //else-> v ?:Store.getDefault<Any>(entity,k)?.value ?:return report(eEntity.ERROR.encode_error,"not initialized encoding Entity ${type.simpleName}.${k}")
                else-> {
                    v ?: run {
                        entity._defaultMap?.get(Indexer.get(entity::class,k))?.value ?:return report(Data.ERROR.encode_error,"not initialized encoding Entity ${type.simpleName}.${k}")
                    }
                }
            }
            if(value != null) Indexer.getOrNull(type,k)?.also{
                val e = if(value == optionalNullValue) "$optionalNullValue" else encode(field::class, value, field, report)
                result[it] = e
            }
            true
        }){
            /*result.forEachIndexed{ index, s ->
                log("----------${index}==[$s]----------")
            }*/
            val r = result.joinToString("|",postfix="|")
            //log("=====${result.size}==[$r]=========")
            r
        }else null
    }
    private inline fun encodeResult(result:String) = (if(result.isNotBlank()) result.substring(1) else "") + "@"
    private inline fun encodeValueMap(v:Any):String {
        var result = ""
        (v as Map<String,*>).forEach{(k,it)-> result += "|" + encodeString(k) + "|" + it.toString() }
        return encodeResult(result)
    }
    private inline fun encodeUnion(it:Any, union:Union<*>, report:Report):String?{
        val type:KClass<out Any> = it::class
        val index:Int = union.type.indexOf(type)
        if(index == -1) return report(Data.ERROR.encode_error,"invalid union subtype. unionType:${union.type},entity:${type.simpleName}")
        val result = encodeEntity(it,report) ?: return null
        return index.toString() + (if(result.isNotBlank()) "|$result" else "")
    }

    /**
     * Decode-------------------------------
     */
    private inline fun <ENTITY: Data> decodeEntity(serial:String, cursor: Cursor, entity:ENTITY, report: Report):ENTITY?{
        val type:KClass<out Data> = entity::class
        //val fields:HashMap<String,Field<*>> = Field[type] ?: return entity
        val fields:HashMap<String,Field<*>> = entity.fields

        if(entity.fields.isEmpty()) {
            if(serial[cursor.v++] == '|') {
                return entity
            } else {
                return report(Data.ERROR.decode_error,"empty entity expected at ${cursor.v-1}:${type.simpleName}")
            }
        }

        val convert:ArrayList<Map.Entry<String,Field<*>>> = ArrayList<Map.Entry<String,Field<*>>>(fields.size).also{ list->repeat(fields.size){list.add(entry)}}
        fields.forEach{
            convert[Indexer.get(type,it.key)] = it
        }
        convert.forEach{
            when{
                serial.length == cursor.v->{}
                serial[cursor.v] == optionalNullValue-> cursor.v++
                else->{
                    val v = decode(it.value, serial, cursor, report) ?: return report(Data.ERROR.decode_error,"no value:${type.simpleName}:${it.key}")
                    try{
                        entity.setRawValue(it.key, v)
                    }catch(e:Error){
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
    private inline fun <T> decodeValueMap(serial:String, cursor:Cursor, report:Report, block:String.()->T?):Any?{
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
    private inline fun decodeStringList(serial:String, cursor:Cursor, report:Report):List<String>?{
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
    private inline fun decodeStringValue(serial:String, cursor:Cursor):String{
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


    /**
     * 필드에 맞는 인코더와 디코더를 호출하여 변환값을 반환함
     */
    private inline fun encode(type:KClass<*>, v:Any, field: Field<*>, report: Report):String{
        return encoders[type]?.invoke(v,field,report) ?: encodeValue(v,field,report)
    }
    private inline fun decode(field: Field<*>, serial:String, cursor: Cursor, report: Report):Any?{
        return decoders[field::class]?.invoke(field,serial,cursor,report)
    }
}