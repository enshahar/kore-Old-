@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package kore.vo.field

interface Field<VALUE:Any>

//object UtcField: Field<eUtc>() {
//    @Suppress("NOTHING_TO_INLINE")
//    inline fun Data.default(noinline factory:(Data)->eUtc){
//        _defaultMap = _defaultMap ?: hashMapOf()
//        _defaultMap!![_index] = DefaultFactoryTask(factory)
//    }
//}
//
//object IntListField: Field<MutableList<Int>>(){
//    inline fun VO.default(noinline factory:(VO)->List<Int>){
//        _task?.default = factory
//    }
//}
//object UIntListField: Field<MutableList<UInt>>(){
//    inline fun VO.default(noinline factory:(VO)->List<UInt>){
//        _task?.default = factory
//    }
//}
//object LongListField: Field<MutableList<Long>>(){
//    inline fun VO.default(noinline factory:(VO)->List<Long>){
//        _task?.default = factory
//    }
//}
//object ULongListField: Field<MutableList<ULong>>(){
//    inline fun VO.default(noinline factory:(VO)->List<ULong>){
//        _task?.default = factory
//    }
//}
//object ShortListField: Field<MutableList<Short>>(){
//    inline fun VO.default(noinline factory:(VO)->List<Short>){
//        _task?.default = factory
//    }
//}
//object UShortListField: Field<MutableList<UShort>>(){
//    inline fun VO.default(noinline factory:(VO)->List<UShort>){
//        _task?.default = factory
//    }
//}
//object FloatListField: Field<MutableList<Float>>(){
//    inline fun VO.default(noinline factory:(VO)->List<Float>){
//        _task?.default = factory
//    }
//}
//object DoubleListField: Field<MutableList<Double>>(){
//    inline fun VO.default(noinline factory:(VO)->List<Double>){
//        _task?.default = factory
//    }
//}
//object BooleanListField: Field<MutableList<Boolean>>(){
//    inline fun VO.default(noinline factory:(VO)->List<Boolean>){
//        _task?.default = factory
//    }
//}
//object StringListField: Field<MutableList<String>>(){
//    inline fun VO.default(noinline factory:(VO)->List<String>){
//        _task?.default = factory
//    }
//}
//
//object IntMapField: Field<HashMap<String, Int>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, Int>){
//        _task?.default = factory
//    }
//}
//object UIntMapField: Field<HashMap<String, UInt>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, UInt>){
//        _task?.default = factory
//    }
//}
//object LongMapField: Field<HashMap<String, Long>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, Long>){
//        _task?.default = factory
//    }
//}
//object ULongMapField: Field<HashMap<String, ULong>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, ULong>){
//        _task?.default = factory
//    }
//}
//object ShortMapField: Field<HashMap<String, Short>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, Short>){
//        _task?.default = factory
//    }
//}
//object UShortMapField: Field<HashMap<String, UShort>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, UShort>){
//        _task?.default = factory
//    }
//}
//object FloatMapField: Field<HashMap<String, Float>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, Float>){
//        _task?.default = factory
//    }
//}
//object DoubleMapField: Field<HashMap<String, Double>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, Double>){
//        _task?.default = factory
//    }
//}
//object BooleanMapField: Field<HashMap<String, Boolean>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, Boolean>){
//        _task?.default = factory
//    }
//}
//object StringMapField: Field<HashMap<String, String>>(){
//    inline fun VO.default(noinline factory:(VO)->Map<String, String>){
//        _task?.default = factory
//    }
//}