package ein2b.core.entity.task

open class
DefaultTask<VALUE:Any>(v:Any){
    @Suppress("UNCHECKED_CAST")
    open val value:VALUE = v as VALUE
}

class DefaultFactoryTask<VALUE:Any>(val factory:()->VALUE): DefaultTask<VALUE>(0){
    override val value:VALUE get() = factory()
}