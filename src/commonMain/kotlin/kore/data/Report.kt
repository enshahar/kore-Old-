package ein2b.core.entity

import kore.error.E

class Report{
    var id:Any? = null
    var message:String? = null
    var result:Array<out Any>? =null

    /**
     * 최초의 에러만 리포트를 작성한다
     */
    operator fun<T> invoke(id:Any?, message:String?, vararg result:Any):T?{
        //log("Report invoke Error : $id : $message")
        if(this.id == null) {
            this.id = id
            this.message = message
            this.result = result
        }
        return null
    }

    fun report(block:((E)->Unit)){
        block.invoke(E(id!!,message!!,*result!!))
    }
}