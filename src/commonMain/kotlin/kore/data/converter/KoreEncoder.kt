@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package kore.data.converter

import kore.data.Data
import kore.data.SlowData
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
    fun encode(type:KClass<*>, v:Any, field: Field<*>):Wrap<String> = encoders[type]?.invoke(v, field) ?: W("$v")
    fun encodeData(d:Any):Wrap<String>{
        val data:Data = d as Data
        val slowData:SlowData? = data as? SlowData
        val fields:HashMap<String, Field<*>> = slowData?._fields ?: Field[data::class] ?: return W(EncodeDataNoFieldAll(data))
        if(fields.isEmpty()) return W("|")
        val values:MutableMap<String, Any?> = data._values ?: return W(EncodeDataNoValue(data))
        if(fields.size != values.size) return W(EncodeDataNoInitialized(data))
        val type:KClass<out Data> = data::class
        val result:ArrayList<String> = ArrayList<String>(fields.size).also{list->repeat(fields.size){list.add("")}}
        values.forEach{ (k,v) ->
            val field:Field<*> = fields[k] ?: return W(EncodeDataNoField(data, k))
            val index:Int = Indexer.get(type, k)() ?: return W(Data.NoIndex(k))
            val task:Task? = slowData?._tasks?.get(index) ?: TaskStore(type, index)
            val include: ((Data) -> Boolean)? = task?.include
            when{
                include == Field.isOptional-> v ?: '~'
                include?.invoke(data) == false-> null
                else-> v ?: task?.getDefault(data) ?:return W(EncodeDataNoField(data, k))
            }?.let{value->
                result[index] = if(value == '~') "~" else{
                    val wrap: Wrap<String> = encode(field::class, value, field)
                    wrap() ?: return W(wrap.value as Throwable)
                }
            }
        }
        return W(result.joinToString("|", postfix="|"))
    }
    private val valueList:(Any, Field<*>)->Wrap<String> = { v, _-> W((v as List<*>).joinToString("|")+"@")}
    private inline fun encodeResult(result:String) = (if(result.isNotBlank()) result.substring(1) else "") + "@"
    private val valueMap:(Any, Field<*>)->Wrap<String> = {v, _ ->
        var result = ""
        (v as Map<String,*>).forEach{(k, it)-> result += "|" + encodeString(k) + "|" + it.toString() }
        W(encodeResult(result))
    }
    internal val encoders:HashMap<KClass<*>,(Any, Field<*>)-> Wrap<String>> = hashMapOf(
        IntListField::class to valueList,
        ShortListField::class to valueList,
        LongListField::class to valueList,
        UIntListField::class to valueList,
        UShortListField::class to valueList,
        ULongListField::class to valueList,
        FloatListField::class to valueList,
        DoubleListField::class to valueList,
        BooleanListField::class to valueList,
        IntMapField::class to valueMap,
        ShortMapField::class to valueMap,
        LongMapField::class to valueMap,
        UIntMapField::class to valueMap,
        UShortMapField::class to valueMap,
        ULongMapField::class to valueMap,
        FloatMapField::class to valueMap,
        DoubleMapField::class to valueMap,
        BooleanMapField::class to valueMap,
        //UtcField::class to { v, _-> (v as? eUtc)?.let{ encodeString(it.toString()) } },
        StringField::class to { v, _-> W(encodeString(v)) },
        StringListField::class to { v, _->(v as List<*>).let{
            if (it.isEmpty()) W("!") else W(it.joinToString("|") {encodeString(it)} + "@")
        }},
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