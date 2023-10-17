//@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
//
//package kore.data.field
//
//import kore.data.VO
//import kotlin.reflect.KClass
//
//class DataField<DATA: VO>(val cls: KClass<DATA>, val factory:()->DATA): Field<DATA>(){
//    companion object{
//        @PublishedApi internal val fields:HashMap<KClass<out VO>, DataField<out VO>> = hashMapOf()
//        inline operator fun <reified DATA: VO> get(noinline factory:()->DATA): DataField<DATA> {
//            return fields.getOrPut(DATA::class){DataField(DATA::class, factory)} as DataField<DATA>
//        }
//        inline operator fun <DATA: VO> get(cls:KClass<DATA>, noinline factory:()->DATA): DataField<DATA> {
//            return fields.getOrPut(cls){DataField(cls, factory)} as DataField<DATA>
//        }
//    }
//    inline fun VO.default(noinline factory:(VO)-> DATA){
//        _task?.default = factory
//    }
//}
//class DataListField<DATA: VO>(val cls: KClass<DATA>, val factory:()->DATA): Field<MutableList<DATA>>(){
//    companion object{
//        @PublishedApi internal val fields:HashMap<KClass<out VO>, DataListField<out VO>> = hashMapOf()
//        inline operator fun <reified DATA: VO> get(noinline factory:()->DATA): DataListField<DATA> {
//            return fields.getOrPut(DATA::class){DataListField(DATA::class, factory)} as DataListField<DATA>
//        }
//    }
//    inline fun VO.default(noinline factory:(VO)->List<DATA>){
//        _task?.default = factory
//    }
//}
//class DataMapField<DATA: VO>(val cls: KClass<DATA>, val factory:()->DATA): Field<MutableMap<String, DATA>>(){
//    companion object{
//        @PublishedApi internal val fields:HashMap<KClass<out VO>, DataMapField<out VO>> = hashMapOf()
//        inline operator fun <reified DATA: VO> get(noinline factory:()->DATA): DataMapField<DATA> {
//            return fields.getOrPut(DATA::class){DataMapField(DATA::class, factory)} as DataMapField<DATA>
//        }
//        inline operator fun <DATA: VO> get(cls:KClass<DATA>, noinline factory:()->DATA): DataMapField<DATA> {
//            return fields.getOrPut(cls){DataMapField(cls, factory)} as DataMapField<DATA>
//        }
//    }
//    inline fun VO.default(noinline factory:(VO)-> Map<String,DATA>){
//        _task?.default = factory
//    }
//}