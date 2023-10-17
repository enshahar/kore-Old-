package kore.data

class Field<VALUE:Any>(private var value:VALUE){
    fun getValue():VALUE = value
    fun setValue(v:VALUE){
        value = v
    }
}
class Member(name:String, age:Int) {
    val name = Field(name)
    val age = Field(age)

}

fun test(){
}