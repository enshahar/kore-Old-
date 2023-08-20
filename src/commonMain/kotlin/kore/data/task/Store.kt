package ein2b.core.entity.task

import kore.data.Data
import ein2b.core.entity.indexer.Indexer
import ein2b.core.validation.eVali
import kotlin.reflect.KClass

class Store{
    companion object{
        private val stores:HashMap<KClass<out Data>, Store> = hashMapOf()
        @Suppress("NOTHING_TO_INLINE")
        private inline fun isFirst(entity: Data):Boolean = entity::class !in stores || entity._index !in stores[entity::class]!!.store
        fun getFirstTasks(entity: Data):Tasks?{
            if(!isFirst(entity)) {
                // first가 아닌 경우에는 스토어에 데이터가 있는 것이므로 태스크를 반환한다.
                // 혹시 여기서 오류가 났다면 이건 뭔가 동시성 문제가 발생한 것으로 밖에 생각할 수 없으므로 예외를 던진다
                val store = stores[entity::class] ?: throw IllegalStateException("Store should include ${entity::class} but not!")
                return store.store[entity._index] ?: throw IllegalStateException("Tasks should include ${entity._index} but not!")
            }
            val store = (stores[entity::class] ?: Store().also { stores[entity::class] = it })
            if (store.maxIndex >= entity._index) return null
            store.maxIndex = entity._index
            return if (entity._index in store.store) null else Tasks().also { store.store[entity._index] = it }
        }
        /* // TODO: 클래스 디폴트와 인스턴스 디폴트 구분?
        fun <T:Any>getDefault(entity:eEntity, name:String):DefaultTask<T>?{
            val type = entity::class
            @Suppress("UNCHECKED_CAST")
            return stores[type]?.store?.get(Indexer.get(type, name))?.default as? DefaultTask<T>
        }*/
        internal fun getInclude(entity: Data, name:String):(()->Boolean)?{
            val type = entity::class
            return stores[type]?.store?.get(Indexer.get(type, name))?.include
        }
        internal fun getVali(entity: Data, name:String):eVali?{
            val type = entity::class
            return stores[type]?.store?.get(Indexer.get(type, name))?.vali
        }
        internal fun getSetTask(entity: Data, name:String):List<ConvertTask>?{
            val type = entity::class
            return stores[type]?.store?.get(Indexer.get(type, name))?.setTasks
        }
        internal fun getGetTask(entity: Data, name:String):List<ConvertTask>?{
            val type = entity::class
            return stores[type]?.store?.get(Indexer.get(type, name))?.getTasks
        }
    }
    private var maxIndex = -1
    private var store:HashMap<Int, Tasks> = hashMapOf()
}