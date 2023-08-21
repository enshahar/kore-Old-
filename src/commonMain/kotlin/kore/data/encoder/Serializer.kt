package ein2b.core.entity.encoder

import kore.data.Data
import kore.error.E


interface Serializer<RESULT:Any>{
    fun serialize(entity: Data, block:((E)->Unit)? = null):RESULT?
    fun <ENTITY: Data>unserialize(entity: ENTITY, value:RESULT, block:((E)->Unit)? = null):ENTITY?
}