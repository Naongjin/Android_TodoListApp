package dduw.com.mobile.finalreport

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
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

        val todoData = intent.getSerializableExtra("edit_todo") as? TodoEntity
        if (todoData != null) {
            targetTodo = todoData
            selectedDate = targetTodo.date ?: ""

            editBinding.editTodo.setText(targetTodo.todo)
            editBinding.editetCategory.setText(targetTodo.category)
            editBinding.edittvDate.text = selectedDate

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

        editBinding.editcvCalendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format(Locale.getDefault(), "%d.%02d.%02d.", year, month + 1, dayOfMonth)
            editBinding.edittvDate.text = selectedDate
        }

        editBinding.editAddBtn.setOnClickListener {
            val todoText = editBinding.editTodo.text.toString()
            val categoryText = editBinding.editetCategory.text.toString().trim()

            //수정창 공백 검증 및 안내 토스트 출력
            if (todoText.isEmpty() || categoryText.isEmpty()) {
                Toast.makeText(this, "수정할 내용과 카테고리를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedTodo = targetTodo.copy(
                todo = todoText,
                date = selectedDate,
                category = categoryText
            )

            CoroutineScope(Dispatchers.IO).launch {
                todoDao.updateTodo(updatedTodo)
            }
            finish()
        }

        editBinding.editCancelBtn.setOnClickListener {
            finish()
        }
    }
}