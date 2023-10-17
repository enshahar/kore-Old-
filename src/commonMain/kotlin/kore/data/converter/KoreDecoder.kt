//@file:Suppress("NOTHING_TO_INLINE")
//
//package kore.data.converter
//
//import kore.data.VO
//import kore.data.converter.KoreConverter.Cursor
//import kore.data.converter.KoreConverter.Cursor.DecodeNoListTeminator
//import kore.data.converter.KoreConverter.OPTIONAL_NULL_C
//import kore.data.converter.KoreConverter.STRINGLIST_EMPTY_C
//import kore.data.converter.KoreConverter.decodeString
//import kore.data.field.*
//import kore.data.indexer.Indexer
//import kore.error.E
//import kore.wrap.W
//import kore.wrap.Wrap
//import kotlin.reflect.KClass
//
//
///**
// * 디코더에서 사용하는 빈 객체
// * get()으로 값을 가져올 수 없음
// */
//private val entry:Map.Entry<String, Field<*>> = object:Map.Entry<String, Field<*>>{
//    override val key:String get() = throw Throwable("DecodeEntry")
//    override val value: Field<*> get() = throw Throwable("DecodeEntry")
//}
////fun getDecodeStringValue(cursor: Cursor):String = KoreDecoder.decodeStringValue(cursor)
//internal object KoreDecoder{
//    class DecodeInvalidEmptyData(val data:VO, val cursor:Int): E(data)
//    class DecodeNoDecoder(val field:Field<*>, val cursor:Int, val encoded:String): E(field, cursor, encoded)
//    class DecodeInvalidValue(val field:Field<*>, val target:String, val cursor:Int): E(field, target, cursor)
//    class DecodeInvalidListValue(val field:Field<*>, val target:String, val index:Int): E(field, target, index)
//    class DecodeInvalidMapValue(val field:Field<*>, val target:String, val key:String): E(field, target, key)
//    private inline fun decode(cursor:Cursor, field: Field<*>):Wrap<Any>{
//        return decoders[field::class]?.invoke(cursor, field) ?: W(DecodeNoDecoder(field, cursor.v, cursor.encoded))
//    }
//    internal fun <DATA: VO> data(cursor:Cursor, data:DATA):Wrap<DATA>{
//        val type:KClass<out VO> = data::class
//        val slowData: SlowData? = data as? SlowData
//        /** 이미 data의 인스턴스를 받았으므로 slow의 _fields나 Field에 있을 수 밖에 없음*/
//        val fields:HashMap<String, Field<*>> = slowData?._fields ?: Field[data::class]!!
//        if(fields.isEmpty()){
//            return if(cursor.getAndNext() == '|') W(data) else W(DecodeInvalidEmptyData(data, cursor.v - 1))
//        }
//        val convert: ArrayList<Map.Entry<String, Field<*>>> = ArrayList(fields.size)
//        repeat(fields.size){convert.add(entry)}
//        fields.forEach{convert[Indexer.get(type, it.key)()!!] = it}
//        convert.forEach{entry->
//            //println("key ${entry.key}, ${entry.value}, ${cursor.isEnd}, ${decoders[entry.value::class]}, ${cursor.v}")
//            when{
//                cursor.isEnd->{}
//                cursor.curr == OPTIONAL_NULL_C -> cursor.v++
//                else-> decode(cursor, entry.value).flatMap{
//                    try{
//                        data.set(entry.key, it)
//                        W(it)
//                    }catch(e:Throwable){
//                        W(e)
//                    }
//                }
//            }
//            cursor.v++
//        }
//        return W(data)
//    }
//    private inline fun<VALUE:Any> value(cursor:Cursor, field: Field<*>, block:String.()->VALUE?):Wrap<VALUE>{
//        val target: String = cursor.nextValue
//        return block(target)?.let{ W(it) } ?: W(DecodeInvalidValue(field, target, cursor.v))
//    }
//    private inline fun <VALUE:Any> valueList(cursor: Cursor, field: Field<*>, crossinline block:String.()->Wrap<VALUE>):Wrap<List<VALUE>>{
//        return cursor.nextValueList.flatMap{list->
//            list.flatMapList(block)
//        }
//    }
//    private inline fun <VALUE:Any> valueMap(cursor:Cursor, field: Field<*>, crossinline block:(String, String)->Wrap<VALUE>):Wrap<HashMap<String, VALUE>>{
//        return stringList(cursor).flatMap{
//            it.flatMapListToMap(block)
//        }
//    }
//    private inline fun stringValue(cursor: Cursor):Wrap<String>{
//        val encoded = cursor.encoded
//        val pin = cursor.v
//        var at = pin
//        do {
//            at = encoded.indexOf('|', at)
//            if (at == -1) { /** encoded 맨 끝까지 문자열인 경우 */
//                cursor.v = encoded.length
//                return W(decodeString(encoded.substring(pin, cursor.v)))
//            } else if(at == pin) return W("") /** 길이 0인 문자열  || 인 상황 */
//            else if (encoded[at-1] == '\\') at++ /** \| 스킵하기 */
//            else {
//                cursor.v = at
//                return W(decodeString(encoded.substring(pin, at)))
//            }
//        } while(true)
//    }
//    private val regStringSplit = """(?<!\\)\|""".toRegex()
//    private inline fun stringList(cursor: Cursor):Wrap<List<String>>{
//        val list:ArrayList<String> = arrayListOf()
//        if(cursor.curr == STRINGLIST_EMPTY_C) { /** !로 마크된 빈 리스트 */
//            cursor.v++
//            return W(list)
//        }
//        val encoded = cursor.encoded
//        val pin = cursor.v
//        var at = pin
//        do{
//            at = encoded.indexOf('@', at)
//            if(at == -1) return W(DecodeNoListTeminator(encoded.substring(pin)))
//            else if(encoded[at - 1] == '\\') at++
//            else break
//        }while(true)
//        cursor.v = at + 1
//        return W(encoded.substring(pin, at).splitToSequence(regStringSplit).map{decodeString(it)}.toList())
//    }
//    internal val decoders:HashMap<KClass<*>,(Cursor, Field<*>)->Wrap<Any>> = hashMapOf(
//        IntField::class to { c, f->value(c, f){toIntOrNull()}},
//        ShortField::class to { c, f->value(c, f){toShortOrNull()}},
//        LongField::class to { c, f->value(c, f){toLongOrNull()}},
//        UIntField::class to { c, f->value(c, f){toUIntOrNull()}},
//        UShortField::class to { c, f->value(c, f){toUShortOrNull()}},
//        ULongField::class to { c, f->value(c, f){toULongOrNull()}},
//        FloatField::class to { c, f->value(c, f){toFloatOrNull()}},
//        DoubleField::class to { c, f->value(c, f){toDoubleOrNull()}},
//        BooleanField::class to { c, f->value(c, f){toBooleanStrictOrNull()}},
//        IntListField::class to { c, f->valueList(c, f){toIntOrNull()?.let{W(it)} ?: W(DecodeInvalidListValue(f, this, c.v))}},
//        ShortListField::class to { c, f->valueList(c, f){toUShortOrNull()?.let{W(it)} ?: W(DecodeInvalidListValue(f, this, c.v))}},
//        LongListField::class to { c, f->valueList(c, f){toLongOrNull()?.let{W(it)} ?: W(DecodeInvalidListValue(f, this, c.v))}},
//        UIntListField::class to { c, f->valueList(c, f){toUIntOrNull()?.let{W(it)} ?: W(DecodeInvalidListValue(f, this, c.v))}},
//        UShortListField::class to { c, f->valueList(c, f){toUShortOrNull()?.let{W(it)} ?: W(DecodeInvalidListValue(f, this, c.v))}},
//        ULongListField::class to { c, f->valueList(c, f){toULongOrNull()?.let{W(it)} ?: W(DecodeInvalidListValue(f, this, c.v))}},
//        FloatListField::class to { c, f->valueList(c, f){toFloatOrNull()?.let{W(it)} ?: W(DecodeInvalidListValue(f, this, c.v))}},
//        DoubleListField::class to { c, f->valueList(c, f){toDoubleOrNull()?.let{W(it)} ?: W(DecodeInvalidListValue(f, this, c.v))}},
//        BooleanListField::class to { c, f->valueList(c, f){toBooleanStrictOrNull()?.let{W(it)} ?: W(DecodeInvalidListValue(f, this, c.v))}},
//        IntMapField::class to { c, f->valueMap(c, f){k, v->v.toIntOrNull()?.let{W(it)} ?: W(DecodeInvalidMapValue(f, k, v))}},
//        ShortMapField::class to { c, f->valueMap(c, f){k, v->v.toUShortOrNull()?.let{W(it)} ?: W(DecodeInvalidMapValue(f, k, v))}},
//        LongMapField::class to { c, f->valueMap(c, f){k, v->v.toLongOrNull()?.let{W(it)} ?: W(DecodeInvalidMapValue(f, k, v))}},
//        UIntMapField::class to { c, f->valueMap(c, f){k, v->v.toUIntOrNull()?.let{W(it)} ?: W(DecodeInvalidMapValue(f, k, v))}},
//        UShortMapField::class to { c, f->valueMap(c, f){k, v->v.toUShortOrNull()?.let{W(it)} ?: W(DecodeInvalidMapValue(f, k, v))}},
//        ULongMapField::class to { c, f->valueMap(c, f){k, v->v.toULongOrNull()?.let{W(it)} ?: W(DecodeInvalidMapValue(f, k, v))}},
//        FloatMapField::class to { c, f->valueMap(c, f){k, v->v.toFloatOrNull()?.let{W(it)} ?: W(DecodeInvalidMapValue(f, k, v))}},
//        DoubleMapField::class to { c, f->valueMap(c, f){k, v->v.toDoubleOrNull()?.let{W(it)} ?: W(DecodeInvalidMapValue(f, k, v))}},
//        BooleanMapField::class to { c, f->valueMap(c, f){k, v->v.toBooleanStrictOrNull()?.let{W(it)} ?: W(DecodeInvalidMapValue(f, k, v))}},
////        UtcField::class to { _, serial, cursor, _-> KoreConverter.decodeStringValue(serial, cursor).let{ eUtc.of(it) } },
//        StringField::class to {c, _->stringValue(c) },
//        StringListField::class to {c, _-> stringList(c)},
//        StringMapField::class to {c, _->
//            stringList(c).map{
//                var key:String? = null
//                it.fold(hashMapOf<String, String>()) {acc, item->
//                    if(key == null) key = item
//                    else{
//                        acc[key!!] = item
//                        key = null
//                    }
//                    acc
//                }
//            }
//        },
//        EnumField::class to {c,f->
//            value(c, f){toIntOrNull()}.map{(f as EnumField<*>).enums[it]}
//        },
//        EnumListField::class to {c, f->
//            val enums = (f as EnumListField<*>).enums
//            valueList(c, f){toIntOrNull()?.let{W(it)} ?: W(DecodeInvalidListValue(f, this, c.v))}.flatMap{
//                it.flatMapList {item->if(enums.size > item) W(enums[item]) else W(DecodeInvalidListValue(f, "$item", c.v))}
//            }
//        },
//        EnumMapField::class to {c, f->
//            val enums = (f as EnumMapField<*>).enums
//            stringList(c).flatMap{
//                it.flatMapListToMap {k, v->
//                    v.toIntOrNull()?.let{index->
//                        if(enums.size > index) W(enums[index]) else W(DecodeInvalidMapValue(f, k, v))
//                    } ?: W(DecodeInvalidMapValue(f, k, v))
//                }
//            }
//        },
//        DataField::class to { c, f->data(c, (f as DataField<*>).factory())},
//        DataListField::class to {c, f->
//            val result: ArrayList<Any> = arrayListOf()
//            val factory: () -> VO = (f as DataListField<*>).factory
//            c.loopItems {
//                data(c, factory()).effect{result.add(it)}
//            } ?: W(result)
//        },
//        DataMapField::class to {c, f->
//            val result:HashMap<String, VO> = hashMapOf()
//            val factory: () -> VO = (f as DataMapField<*>).factory
//            c.loopItems {
//                stringValue(c).flatMap{ key->
//                    c.v++
//                    data(c, factory()).effect{result[key] = it}
//                }
//            } ?: W(result)
//        },
//        UnionField::class to {c, f->
//            value(c, f){toIntOrNull()}.flatMap{ index->
//                val factory:()->VO = (f as UnionField<*>).union.factories[index]
//                c.v++
//                data(c, factory())
//            }
//        },
//        UnionListField::class to {c, f->
//            val result:ArrayList<Any> = arrayListOf()
//            val factories:Array<out ()->VO> = (f as UnionListField<*>).union.factories
//            c.loopItems {
//                value(c, f){toIntOrNull()}.map{
//                    factories[it]
//                }.flatMap{ factory->
//                    c.v++
//                    data(c, factory()).effect{
//                        result.add(it)
//                    }
//                }
//            }?.let{W(it)} ?: W(result)
//        },
//        UnionMapField::class to {c, f->
//            val result:HashMap<String, VO> = hashMapOf()
//            val factories = (f as UnionMapField<*>).union.factories
//            c.loopItems {
//                stringValue(c).flatMap{ key->
//                    c.v++
//                    value(c, f){toIntOrNull()}.map{factories[it]()}.flatMap{
//                        c.v++
//                        data(c, it).effect{
//                            result[key] = it
//                        }
//                    }
//                }
//            } ?: W(result)
//        },
//    )
//}