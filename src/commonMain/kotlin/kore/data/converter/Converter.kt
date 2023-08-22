package kore.data.converter

import kore.data.Data
import kore.wrap.Wrap


interface Converter<RESULT:Any>{
    fun toOther(data:Data):Wrap<RESULT>
    fun <DATA: Data>toData(data:DATA, value:RESULT):Wrap<DATA>
}