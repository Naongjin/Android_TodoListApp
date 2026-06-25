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
import dduw.com.mobile.finalreport.databinding.ActivityAddBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddActivity : AppCompatActivity() {
    val TAG = "AddActivity"

    val addBinding by lazy{
        ActivityAddBinding.inflate(layoutInflater)
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
        setContentView(addBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        addBinding.addcvCalendar.setOnDateChangeListener {
            _,year,month, dayOfMonth ->
            selectedDate = String.format(Locale.getDefault(), "%d.%02d.%02d.", year, month + 1, dayOfMonth)
            addBinding.addtvDate.text = selectedDate
        }
        addBinding.addtvDate.text = selectedDate

        addBinding.addAddBtn.setOnClickListener {
            val todoText = addBinding.addTodo.text.toString().trim()
            val categoryText = addBinding.addetCategory.text.toString().trim()

            // 필수 입력 검증 및 안내 토스트 출력
            if (todoText.isEmpty() || categoryText.isEmpty()) {
                Toast.makeText(this, "할 일과 카테고리를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                todoDao.insertTodo(
                    TodoEntity(0, todoText, "food01", selectedDate, false, categoryText) // 🎯 category 실어서 인서트
                )
            }
            finish()
        }

        addBinding.addCancelBtn.setOnClickListener {
            finish()
        }
    }
}