package dduw.com.mobile.finalreport

data class TodoDto(var photo: Int, var todo: String, val todoDate: Int, var isComplete: Boolean){
    override fun toString(): String = "${todo}01"

}
