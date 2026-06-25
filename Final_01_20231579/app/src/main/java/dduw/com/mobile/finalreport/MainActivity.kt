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

    // 리사이클러뷰 제어용 멤버 변수들
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
                finishAffinity()
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

        // 1. 어댑터 인스턴스 생성 및 리사이클러뷰 연결
        adapter = TodoAdapter()
        binding.rvTodo.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        binding.rvTodo.adapter = adapter

        // 2. Chip 클릭 실시간 DB 갱신 리스너 연결
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

        // 3. 처음 화면이 켜졌을 때는 기본값(오늘 날짜) 세팅 및 데이터 감시 시작
        binding.tvDate.text = selectedDate
        fetchTodosByDate(selectedDate)

        // 4. 사용자가 달력을 클릭해 날짜를 바꿀 때마다 해당 날짜 데이터로 수위칭
        binding.cvCalendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format(Locale.getDefault(), "%d.%02d.%02d.", year, month + 1, dayOfMonth)
            binding.tvDate.text = selectedDate

            // 달력 바뀔 때마다 새 파이프라인 구독
            fetchTodosByDate(selectedDate)
        }
    }

    // 날짜별 투두 데이터를 실시간으로 가져오는 독립 함수 (안전하게 onCreate 밖에 배치)
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