package dduw.com.mobile.finalreport.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity ("todo_table")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name="todo_name") var todo: String?,
    @ColumnInfo(name="todo_photo") var photo: Int?,
    @ColumnInfo(name="todo_date") var date: String?,
    @ColumnInfo(name="todo_isComplete") var isComplete: Boolean?
)
