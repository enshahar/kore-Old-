package ein2b.core.entity.task

import ein2b.core.validation.eVali
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass


class Tasks{
    var vali:eVali? = null
    // TODO: 원래 의도는 클래스 수준의 상수 디폴트만 허용하는 것인데
    // 쓰는 형태롤 보니 어쩔 수 없이 인스턴스 수준으로 내려야 할 것 같음.
    // + map/list 의 경우 초기화가 안되면 빈 리스트도 생기지 않기 떄문에 default를 설정하거나 해줘야 함
    // 두 개념이 헷깔리지 않게 처리하도록 설계를 바꾸고
    // default(mutableListOf(1,2,3))같은 처리를 하지 않도록 처리 방식을 바꿔야 할듯.
    //var default:DefaultTask<*>? = null
    var setTasks:ArrayList<ConvertTask>? = null
    var getTasks:ArrayList<ConvertTask>? = null
    var include:(()->Boolean)? = null
}