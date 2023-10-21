@file:Suppress("NOTHING_TO_INLINE")

package kore.vo.task

import kore.vo.VO
import kore.error.E

abstract class Task{
    fun interface Default:(VO, String)->Any?
    class NoDefault(val vo:VO, val name:String):E(name)
    class TaskFail(val type:String, val vo:VO, val key:String, val result:Any):E(result)
    companion object{
        private val _include:(String, Any?)->Boolean = {_, _->true}
        private val _exclude:(String, Any?)->Boolean = {_, _->false}
        private val _optinal:(String, Any?)->Boolean = {_, v->v != null}
    }
    @PublishedApi internal var _default:Any? = null
    @PublishedApi internal var _setTasks:ArrayList<(VO, String, Any)->Any?>? = null
        internal set
    @PublishedApi internal var _getTasks:ArrayList<(VO, String, Any)->Any?>? = null
        internal set
    var include:(String, Any?)->Boolean = _include
        internal set
    inline fun getDefault(vo:VO, key:String):Any = _default?.let{
        when(it){
            is Default-> it(vo, key)
            else->it
        }?.also {v->
            vo[key] = v
            vo.values[key]
        }
    } ?: NoDefault(vo, key).terminate()
    inline fun getTask(noinline block:(VO, String, Any)->Any?){
        (_getTasks ?: arrayListOf<(VO, String, Any)->Any?>().also { _getTasks = it }).add(block)
    }
    inline fun setTask(noinline block:(VO, String, Any)->Any?){
        (_setTasks ?: arrayListOf<(VO, String, Any)->Any?>().also { _setTasks = it }).add(block)
    }
    inline fun getFold(vo:VO, key:String, v:Any):Any? = _getTasks?.fold(v){ acc, next->
        next(vo, key, acc) ?: TaskFail("get", vo, key, acc).terminate()
    }
    inline fun setFold(vo:VO, key:String, v:Any):Any? = _setTasks?.fold(v){ acc, next->
        next(vo, key, acc) ?: TaskFail("set", vo, key, acc).terminate()
    }
    fun exclude(){include = _exclude}
    fun optinal(){include = _optinal}
    fun isInclude(block:(String, Any?)->Boolean){include = block}
    fun default(block:Default){_default = block}
}