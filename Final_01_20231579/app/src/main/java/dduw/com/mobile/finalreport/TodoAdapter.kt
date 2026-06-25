package dduw.com.mobile.finalreport

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dduw.com.mobile.finalreport.databinding.ListItemBinding

class TodoAdapter(val todoList: MutableList<TodoDto>): RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {
    val TAG = "TodoAdapter"

    override fun getItemCount(): Int = todoList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return TodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todos = todoList[position]
        holder.binding.chipTodo.isChecked = todos.isComplete
        holder.binding.tvTodo.text = todos.todo
        holder.binding.ivPhoto.setImageResource(todos.photo)
    }

    inner class TodoViewHolder(val binding: ListItemBinding): RecyclerView.ViewHolder(binding.root){

    }
}