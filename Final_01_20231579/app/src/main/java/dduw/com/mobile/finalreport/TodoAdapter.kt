package dduw.com.mobile.finalreport

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dduw.com.mobile.finalreport.data.TodoEntity
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

    private fun getResource(image: String): Int{
        return when(image){
            "food01" -> R.drawable.food01
            "food02" -> R.drawable.food02
            "food03" -> R.drawable.food03
            "food04" -> R.drawable.food04

            else -> R.drawable.food01
        }
    }

    inner class TodoViewHolder(val binding: ListItemBinding): RecyclerView.ViewHolder(binding.root){
        init{
            binding.root.setOnClickListener {
                clickListener?.let{
                    it(bindingAdapterPosition)
                }
            }
        }

    }

    val clickListener: ((pos: Int) -> Unit) ?= null

    companion object{
        val callback = object: DiffUtil.ItemCallback<TodoEntity>(){
            override fun areItemsTheSame(
                oldItem: TodoEntity,
                newItem: TodoEntity
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: TodoEntity,
                newItem: TodoEntity
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}