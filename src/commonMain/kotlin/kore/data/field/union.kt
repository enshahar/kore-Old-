//@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
//
//package kore.data.field
//
//import kore.data.VO
//import kore.data.Union
//import kotlin.reflect.KClass
//
//
//class UnionField<DATA: VO>(val union: Union<DATA>): Field<DATA>(){
//    companion object{
//        @PublishedApi internal val fields:HashMap<KClass<out VO>, UnionField<out VO>> = hashMapOf()
//        inline operator fun <reified DATA: VO> get(union: Union<DATA>): UnionField<DATA> {
//            return fields.getOrPut(DATA::class){UnionField(union)} as UnionField<DATA>
//        }
//    }
//}
//class UnionListField<DATA: VO>(val union: Union<DATA>): Field<MutableList<DATA>>(){
//    companion object{
//        @PublishedApi internal val fields:HashMap<KClass<out VO>, UnionListField<out VO>> = hashMapOf()
//        inline operator fun <reified DATA: VO> get(union: Union<DATA>): UnionListField<DATA> {
//            return fields.getOrPut(DATA::class){UnionListField(union)} as UnionListField<DATA>
//        }
//    }
//    inline fun VO.default(noinline factory:(VO)->List<DATA>){
//        _task?.default = factory
//    }
//}
//class UnionMapField<DATA: VO>(val union: Union<DATA>): Field<MutableMap<String, DATA>>(){
//    companion object{
//        @PublishedApi internal val fields:HashMap<KClass<out VO>, UnionMapField<out VO>> = hashMapOf()
//        inline operator fun <reified DATA: VO> get(union: Union<DATA>): UnionMapField<DATA> {
//            return fields.getOrPut(DATA::class){UnionMapField(union)} as UnionMapField<DATA>
//        }
//    }
//    inline fun VO.default(noinline factory:(VO)->Map<String,DATA>){
//        _task?.default = factory
//    }
//}