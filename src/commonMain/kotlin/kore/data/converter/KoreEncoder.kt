@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package kore.data.converter

import kore.data.VO
import kore.data.Union
import kore.data.converter.KoreConverter.STRINGLIST_EMPTY
import kore.data.converter.KoreConverter.OPTIONAL_NULL
import kore.data.converter.KoreConverter.encodeString
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
    class EncodeDataNoFieldAll(val data:VO): E(data)
    class EncodeDataNoField(val data:VO, val field:String): E(data, field)
    class EncodeDataNoValue(val data:VO): E(data)
    class EncodeDataNoInitialized(val data:VO): E(data)
    class EncodeNoEnum(val enums:Array<*>, val value:Any): E(enums, value)
    class EncodeInvalidUnion(val union: Union<*>, val it:Any):E(union, it)
    private inline fun encode(type:KClass<*>, v:Any, field: Field<*>):Wrap<String> = encoders[type]?.invoke(v, field) ?: W("$v")
    fun data(d:Any):Wrap<String>{
        val data:VO = d as VO
        val slowData:SlowData? = data as? SlowData
        val fields:HashMap<String, Field<*>> = slowData?._fields ?: Field[data::class] ?: return W(EncodeDataNoFieldAll(data))
        if(fields.isEmpty()) return W("|")
        val values:MutableMap<String, Any?> = data._values ?: return W(EncodeDataNoValue(data))
        if(fields.size != values.size) return W(EncodeDataNoInitialized(data))
        val type:KClass<out VO> = data::class
        val result:ArrayList<String> = ArrayList(fields.size)
        repeat(fields.size){result.add("")}
        values.forEach{ (k,v) ->
            val field:Field<*> = fields[k] ?: return W(EncodeDataNoField(data, k))
            val index:Int = Indexer.get(type, k)() ?: return W(VO.NoIndex(k))
            val task:Task? = slowData?._tasks?.get(index) ?: TaskStore(type, index)
            val include: ((VO) -> Boolean)? = task?.include
            when{
                include == Field.isOptional-> v ?: OPTIONAL_NULL
                include?.invoke(data) == false-> null
                else-> v ?: task?.getDefault(data) ?:return W(EncodeDataNoField(data, k))
            }?.let{value->
                if(value == OPTIONAL_NULL) result[index] = OPTIONAL_NULL
                else encode(field::class, value, field).effect {
                    result[index] = it
                }
            }
        }
        return W(result.joinToString("|", postfix="|"))
    }
    private inline fun union(it:Any, union: Union<*>):Wrap<String>{
        val type:KClass<out Any> = it::class
        val index:Int = union.type.indexOf(type)
        return if(index == -1) W(EncodeInvalidUnion(union, it))
        else data(it).map{
            index.toString() + (if(it.isNotBlank()) "|$it" else "")
        }
    }

    private inline fun result(result:String) = (if(result.isNotBlank()) result.substring(1) else "") + "@"
    private val valueList:(Any, Field<*>)->Wrap<String> = { v, _-> W((v as List<*>).joinToString("|")+"@")}
    private val valueMap:(Any, Field<*>)->Wrap<String> = {v, _ ->
        var result = ""
        (v as Map<String,*>).forEach{(k, it)-> result += "|" + encodeString(k) + "|" + it.toString() }
        W(result(result))
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
            if (it.isEmpty()) W(STRINGLIST_EMPTY) else W(it.joinToString("|") {encodeString(it)} + "@")
        }},
        StringMapField::class to { v, _->
            var result = ""
            (v as Map<String,*>).forEach{(k,it)->result += "|" + encodeString(k) + "|" + encodeString(it)}
            W(result(result))
        },
        EnumField::class to { v, field->
            val enums:Array<*> = (field as EnumField<*>).enums
            val index:Int = enums.indexOf(v)
            if(index != -1) W("$index") else W(EncodeNoEnum(enums, v))
        },
        EnumListField::class to { v, field ->
            val enums: Array<*> = (field as EnumListField<*>).enums
            var result: String = ""
            var error:Any? = null
            if ((v as List<*>).all { e ->
                val index:Int = enums.indexOf(e)
                if(index == -1) {
                    error = e
                    false
                }else{
                    result += "|$index"
                    true
                }
            }) W(result(result)) else W(EncodeNoEnum(enums, error!!))
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
                    result += "|" + encodeString(k) + "|" + index.toString()
                    true
                }
            }) W(result(result)) else W(EncodeEnum(enums, error!!))
        },
        DataField::class to { v, _-> data(v) },
        DataListField::class to { v, _->
            var result = ""
            var error:Throwable? = null
            if((v as List<*>).all { e ->
                data(e!!).isEffected{ result += "|$it" }?.let{error = it} == null
            }) W(result(result)) else W(error!!)
        },
        DataMapField::class to { v, _->
            var result = ""
            var error:Throwable? = null
            if((v as Map<String, *>).all { (k, it) ->
                data(it!!).isEffected{ result += "|${encodeString(k)}|$it" }?.let{error = it} == null
            }) W(result(result)) else W(error!!)
        },
        UnionField::class to { v, field-> union(v, (field as UnionField<*>).union) },
        UnionListField::class to { v, field->
            val un: Union<VO> = (field as UnionListField<*>).union
            var result = ""
            var error:Throwable? = null
            if((v as List<*>).all{ e ->
                union(e!!,un).isEffected{ result += "|$it" }?.let{error = it} == null
            }) W(result(result)) else W(error!!)
        },
        UnionMapField::class to { v, field->
            var result = ""
            val un: Union<VO> = (field as UnionMapField<*>).union
            var error:Throwable? = null
            if((v as Map<String, *>).all{ (k, it) ->
                union(it!!, un).isEffected{ result += "|${encodeString(k)}|$it" }?.let{error = it} == null
            }) W(result(result)) else W(error!!)
        }
    )
}