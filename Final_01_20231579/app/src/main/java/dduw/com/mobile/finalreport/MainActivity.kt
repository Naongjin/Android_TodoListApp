package dduw.com.mobile.finalreport

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
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
    val todoDao by lazy{
        TodoDatabase.getDatabase(this).todoDao()
    }

    private var selectedDate: String = SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault()).format(
        Calendar.getInstance().time)

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
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

        val formatter = SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault())
        val dateString = formatter.format(Calendar.getInstance().time)

        val adapter = TodoAdapter()
        binding.rvTodo.layoutManager = LinearLayoutManager(this).apply{
            orientation = LinearLayoutManager.VERTICAL
        }
        binding.rvTodo.adapter = adapter

        binding.tvDate.text = selectedDate
        binding.cvCalendar.setOnDateChangeListener {
                _,year,month, dayOfMonth ->
            selectedDate = String.format(Locale.getDefault(), "%d.%02d.%02d.", year, month + 1, dayOfMonth)
            binding.tvDate.text = selectedDate
        }

        // 기존 lifecycleScope를 Job을 사용하여 앱의 기능(해당 날짜의 투두리스트 표시)에 맞게 심화시켰습니다.
        /*
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                val listFlow = todoDao.getTodos()
                listFlow.distinctUntilChanged().collect {
                    todos -> adapter.submitList(todos)
                }
            }
        }
        */
        var todoCollectJob: kotlinx.coroutines.Job? = null
        fun fetchTodosByDate(date: String){
            // 새로운 날짜를 선택하면, 이전 날짜를 감시하던 코루틴은 취소(cancel)합니다.
            todoCollectJob?.cancel()

            // 지정된 날짜로 DB 파이프라인을 새로 연결합니다.
            todoCollectJob = lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED){
                    todoDao.getTodosByDate(date) // TodoDao에서 만든 쿼리 메서드 호출
                        .distinctUntilChanged()
                        .collect { todos ->
                            adapter.submitList(todos)
                        }
                }
            }
        }

        // 1. 처음 화면이 켜졌을 때는 기본값(오늘 날짜) 데이터 로드
        binding.tvDate.text = selectedDate
        fetchTodosByDate(selectedDate)

        // 2. 사용자가 달력을 클릭해 날짜를 바꿀 때마다 해당 날짜 데이터로 변경
        binding.cvCalendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format(Locale.getDefault(), "%d.%02d.%02d.", year, month + 1, dayOfMonth)
            binding.tvDate.text = selectedDate

            // 달력 날짜를 바꿀 때마다 호출하면 하단의 리사이클러뷰의 내용이 바뀜
            fetchTodosByDate(selectedDate)
        }

    }
}