package dduw.com.mobile.finalreport

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dduw.com.mobile.finalreport.data.TodoDatabase
import dduw.com.mobile.finalreport.data.TodoEntity
import dduw.com.mobile.finalreport.databinding.ActivityEditBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditActivity : AppCompatActivity() {
    val TAG = "EditActivity"

    val editBinding by lazy {
        ActivityEditBinding.inflate(layoutInflater)
    }
    val todoDao by lazy {
        TodoDatabase.getDatabase(this).todoDao()
    }

    // 수정할 대상 투두 객체를 저장할 지연 초기화 변수
    private lateinit var targetTodo: TodoEntity
    private var selectedDate: String = ""

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
        setContentView(editBinding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. MainActivity가 보내준 원본 투두 객체를 안전하게 수령합니다.
        val todoData = intent.getSerializableExtra("edit_todo") as? TodoEntity
        if (todoData != null) {
            targetTodo = todoData
            selectedDate = targetTodo.date ?: ""

            // 🎯 2. 받아온 원본 데이터를 수정 화면 뷰들에 세팅(복원)해 줍니다.
            editBinding.editTodo.setText(targetTodo.todo)
            editBinding.edittvDate.text = selectedDate

            // 캘린더뷰 날짜도 기존 등록된 날짜로 동기화 시켜줍니다.
            try {
                val sdf = SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault())
                val date = sdf.parse(selectedDate)
                if (date != null) {
                    editBinding.editcvCalendar.date = date.time
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // 혹시라도 데이터가 안 넘어왔을 때를 대비한 방어 코드
            selectedDate = SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault()).format(Calendar.getInstance().time)
            editBinding.edittvDate.text = selectedDate
        }

        // 3. 달력 날짜 변경 리스너 연동 (edit 전용 ID 반영)
        editBinding.editcvCalendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format(Locale.getDefault(), "%d.%02d.%02d.", year, month + 1, dayOfMonth)
            editBinding.edittvDate.text = selectedDate
        }

        // 4. [수정 버튼 클리 리스너]
        editBinding.editAddBtn.setOnClickListener {
            val todoText = editBinding.editTodo.text.toString()

            // 기존 고유 id와 체크 상태(isComplete)를 그대로 유지한 채 알맹이만 바꾼 새 객체를 생성합니다.
            val updatedTodo = targetTodo.copy(
                todo = todoText,
                date = selectedDate
            )

            CoroutineScope(Dispatchers.IO).launch {
                // insertTodo 대신 updateTodo를 쳐서 기존 행을 덮어씁니다!
                todoDao.updateTodo(updatedTodo)
            }
            finish()
        }

        // 5. 취소 버튼 (edit 전용 ID 반영)
        editBinding.editCancelBtn.setOnClickListener {
            finish()
        }
    }
}