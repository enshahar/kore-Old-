package kore.data.converter

import kore.data.Data
import kore.wrap.Wrap


interface Converter<RESULT:Any>{
    fun encode(data:Data):Wrap<RESULT>
    fun <DATA: Data>decode(data:DATA, value:RESULT):Wrap<DATA>
}