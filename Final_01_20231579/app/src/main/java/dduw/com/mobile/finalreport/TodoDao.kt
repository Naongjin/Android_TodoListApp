package dduw.com.mobile.finalreport

class TodoDao {
    private val _todos = mutableListOf<TodoDto>()

    val todos: List<TodoDto>
        get() = _todos

    fun addTodo(todo: TodoDto) = _todos.add(todo)
    fun removeTodo(index: Int) = _todos.removeAt(index)
    fun setTodo(index: Int, todo: TodoDto) = _todos.set(index, todo)

    init{
        _todos.add(TodoDto(R.drawable.food01, "양치질하기", 260625, false) )
    }
}