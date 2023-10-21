package kore.vo.converter

import kore.vo.VO
import kore.wrap.Wrap


interface Converter<RESULT:Any>{
    fun to(data:VO):Wrap<RESULT>
    fun <V:VO>from(data:V, value:RESULT):Wrap<V>
}