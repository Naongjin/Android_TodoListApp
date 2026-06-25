package dduw.com.mobile.finalreport.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Insert
    suspend fun insertTodo(vararg todo: TodoEntity)

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)

    @Query("SELECT * FROM todo_table")
    fun getTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todo_table WHERE todo_date = :selectedDate ORDER BY todo_isComplete ASC")
    fun getTodosByDate(selectedDate: String): Flow<List<TodoEntity>>
}