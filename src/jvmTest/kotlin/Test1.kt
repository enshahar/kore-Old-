import kotlin.test.Test
import kotlin.test.assertEquals


class Field<VALUE:Any>(private var value:VALUE){
    fun getValue():VALUE = value
    fun setValue(v:VALUE){value = v}
}
class Member(name:String, age:Int){
    val name = Field(name)
    val age = Field(age)
}

class Test1 {
    @Test
    fun test1(){
        val member = Member("hika", 20)
        assertEquals(member.name.getValue(), "hika")
        assertEquals(member.age.getValue(), 20)
        member.name.setValue("boeun")
        member.age.setValue(10)
        assertEquals(member.name.getValue(), "boeun")
        assertEquals(member.age.getValue(), 10)
    }
}