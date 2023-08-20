package ein2b.core.entity.encoder

import kore.data.Data
import ein2b.core.entity.Error


interface Serializer<RESULT:Any>{
    fun serialize(entity: Data, block:((Error)->Unit)? = null):RESULT?
    fun <ENTITY: Data>unserialize(entity: ENTITY, value:RESULT, block:((Error)->Unit)? = null):ENTITY?
}