package dduw.com.mobile.finalreport

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dduw.com.mobile.finalreport.data.TodoDatabase
import dduw.com.mobile.finalreport.data.TodoEntity
import dduw.com.mobile.finalreport.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    val todoDao by lazy {
        TodoDatabase.getDatabase(this).todoDao()
    }

    private var selectedDate: String = SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault()).format(
        Calendar.getInstance().time
    )

    private lateinit var adapter: TodoAdapter
    private var todoCollectJob: Job? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add -> {
                val intent = Intent(this, AddActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.info -> {
                val intent = Intent(this, InfoActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.off -> {
                AlertDialog.Builder(this).apply {
                    setTitle("앱 종료")
                    setMessage("앱을 종료하시겠습니까?")
                    setPositiveButton("종료") { _, _ -> finishAffinity() }
                    setNegativeButton("취소") { dialog, _ -> dialog.dismiss() }
                    create()
                    show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = TodoAdapter()
        binding.rvTodo.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        binding.rvTodo.adapter = adapter

        adapter.checkListener = { updatedTodo ->
            CoroutineScope(Dispatchers.IO).launch {
                todoDao.updateTodo(updatedTodo)
            }
        }

        adapter.clickListener = {pos ->
            val todoItem = adapter.currentList[pos]
            val intent = Intent(this, EditActivity::class.java)
            intent.putExtra("edit_todo", todoItem)
            startActivity(intent)
        }
        adapter.longCheckListener = { targetTodo ->
            val builder: AlertDialog.Builder = AlertDialog.Builder(this).apply {
                setTitle("할 일 삭제")
                setMessage("${targetTodo.todo}\n할 일을 삭제하시겠습니까?")
                setPositiveButton("삭제"){ dialog, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        todoDao.deleteTodoById(targetTodo.id)
                    }
                    dialog.dismiss()
                }
                setNegativeButton("취소"){ dialog, _ ->
                    dialog.dismiss()
                }
            }
            val dialog = builder.create()
            dialog.show()
        }

        binding.tvDate.text = selectedDate
        fetchTodosByDate(selectedDate)

        binding.cvCalendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format(Locale.getDefault(), "%d.%02d.%02d.", year, month + 1, dayOfMonth)
            binding.tvDate.text = selectedDate

            fetchTodosByDate(selectedDate)
        }
    }

    private fun fetchTodosByDate(date: String) {
        todoCollectJob?.cancel()
        todoCollectJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                todoDao.getTodosByDate(date)
                    .distinctUntilChanged()
                    .collect { todos ->
                        adapter.submitList(todos)
                    }
            }
        }
    }
}