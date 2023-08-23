@file:Suppress("NOTHING_TO_INLINE")

package kore.data.converter

import kore.data.Data
import kore.data.field.*
import kore.data.indexer.Indexer
import kore.data.task.Task
import kore.data.task.TaskStore
import kore.error.E
import kore.wrap.W
import kore.wrap.Wrap
import kotlin.reflect.KClass

internal object KoreEncoder{
    class EncodeEnum(val enums:Array<*>, val value:Any):E(value)
    class EncodeDataNoFieldAll(val data:Data): E(data)
    class EncodeDataNoField(val data:Data, val field:String): E(data, field)
    class EncodeDataNoValue(val data:Data): E(data)
    class EncodeDataNoInitialized(val data:Data): E(data)
    fun encodeData(d:Any):Wrap<String>{
        val data:Data = d as Data
        val fields:HashMap<String, Field<*>> = Field[data::class] ?: return W(EncodeDataNoFieldAll(data))
        if(fields.isEmpty()) return W("|")
        val values:MutableMap<String, Any?> = data._values ?: return W(EncodeDataNoValue(data))
        if(fields.size != values.size) return W(EncodeDataNoInitialized(data))
        val type:KClass<out Data> = data::class
        val result:ArrayList<String> = ArrayList<String>(fields.size).also{list->repeat(fields.size){list.add("")}}
        values.forEach{ (k,v) ->
            val field:Field<*> = fields[k] ?: return W(EncodeDataNoField(data, k))
            val task: Task? = TaskStore(type, k)
            val include: ((Data) -> Boolean)? = task?.include
            when{
                include == Field.isOptional-> v ?: '~'
                include?.invoke(data) == false-> null
                else-> v ?: task?.getDefault(data) ?:return W(EncodeDataNoField(data, k))
            }?.let{value->
                Indexer.get(type, k)()?.let{index->
                    result[index] = if(value == '~') "~" else{
                        val wrap = encode(field::class, value, field)
                        wrap() ?: return W(wrap.value as Throwable)
                    }
                }
            }
        }
        return W(result.joinToString("|", postfix="|"))
    }
    private val encodeValue:(Any, Field<*>)->Wrap<String> = { v, _-> W("$v") }
    fun encode(type:KClass<*>, v:Any, field: Field<*>):Wrap<String>{
        return encoders[type]?.invoke(v, field) ?: encodeValue(v, field)
    }
    internal val encoders:HashMap<KClass<*>,(Any, Field<*>)-> Wrap<String>> = hashMapOf(
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
        IntMapField::class to { v, _-> W(KoreConverter.encodeValueMap(v)) },
        ShortMapField::class to { v, _-> W(KoreConverter.encodeValueMap(v)) },
        LongMapField::class to { v, _-> W(KoreConverter.encodeValueMap(v)) },
        UIntMapField::class to { v, _-> W(KoreConverter.encodeValueMap(v)) },
        UShortMapField::class to { v, _-> W(KoreConverter.encodeValueMap(v)) },
        ULongMapField::class to { v, _-> W(KoreConverter.encodeValueMap(v)) },
        FloatMapField::class to { v, _-> W(KoreConverter.encodeValueMap(v)) },
        DoubleMapField::class to { v, _-> W(KoreConverter.encodeValueMap(v)) },
        BooleanMapField::class to { v, _-> W(KoreConverter.encodeValueMap(v)) },

        //UtcField::class to { v, _-> (v as? eUtc)?.let{ encodeString(it.toString()) } },
        StringField::class to { v, _-> W(KoreConverter.encodeString(v)) },
        StringListField::class to { v, _->(v as List<*>).let{
            W(if (it.isEmpty()) "${KoreConverter.emptyStringListValue}" else it.joinToString("|") {
                KoreConverter.encodeString(
                    it
                )
            } + "@"
        })
        },
        StringMapField::class to { v, _->
            var result = ""
            (v as Map<String,*>).forEach{(k,it)->result += "|" + KoreConverter.encodeString(k) + "|" + KoreConverter.encodeString(
                it
            )
            }
            W(KoreConverter.encodeResult(result))
        },
        EnumField::class to { v, field->
            val enums:Array<*> = (field as EnumField<*>).enums
            val index:Int = enums.indexOf(v)
            if(index != -1) W("$index") else W(Data.EncodeEnum(enums, v))
        },
        EnumListField::class to { v, field ->
            val enums: Array<*> = (field as EnumListField<*>).enums
            var result: String = ""
            var error:Any? = null
            if ((v as List<*>).all { e ->
                    val index: Int = enums.indexOf(e)
                    if(index == -1) {
                        error = e
                        false
                    }else{
                        result += "|$index"
                        true
                    }
                }) W(KoreConverter.encodeResult(result)) else W(Data.EncodeEnum(enums, error!!))
        },
        EnumMapField::class to { v, field->
            val enums:Array<*> = (field as EnumMapField<*>).enums
            var result:String = ""
            var error:Any? = null
            if ((v as Map<String,*>).all { (k,e)->
                    val index: Int = enums.indexOf(e)
                    if (index == -1) {
                        error = e
                        false
                    } else {
                        result += "|" + KoreConverter.encodeString(k) + "|" + index.toString()
                        true
                    }
                }) W(KoreConverter.encodeResult(result)) else W(Data.EncodeEnum(enums, error!!))
        },
//        DataField::class to { v, _-> W(encodeEntity(v)) },
//        DataListField::class to { v, _, r->
//            var result = ""
//            var error:Any? = null
//            if((v as List<*>).all { e -> encodeEntity(e!!)?.let { result+="|$it" } != null}) encodeResult(result) else null
//        },
//        DataMapField::class to { v, _, r->
//            var result = ""
//            if ((v as Map<String, *>).all { (k, it) ->
//                    encodeEntity(it!!, r)?.let { value ->
//                        result += "|" + encodeString(k) + "|" + value
//                    } != null
//                }
//            ) encodeResult(result) else null
//        },
//        UnionField::class to { v, field, r-> encodeUnion(v,(field as UnionField<*>).union,r) },
//        UnionListField::class to { v, field, r->
//            val un: Union<Data> = (field as UnionListField<*>).union
//            var result = ""
//            if((v as List<*>).all{ e -> encodeUnion(e!!,un,r)?.let{ result += "|$it" } != null}) encodeResult(result) else null
//        },
//        UnionMapField::class to { v, field, r->
//            var result = ""
//            val un: Union<Data> = (field as UnionMapField<*>).union
//            if((v as Map<String, *>).all{ (k, it) ->
//                    encodeUnion(it!!, un, r)?.let{ value ->
//                        result += "|" + encodeString(k) + "|" + value
//                    } != null
//                }
//            ) encodeResult(result) else null
//        }
    )
}