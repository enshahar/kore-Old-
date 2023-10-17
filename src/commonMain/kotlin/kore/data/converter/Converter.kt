package kore.data.converter

import kore.data.VO
import kore.wrap.Wrap


interface Converter<RESULT:Any>{
    fun encode(data:VO):Wrap<RESULT>
    fun <DATA:VO>decode(data:DATA, value:RESULT):Wrap<DATA>
}