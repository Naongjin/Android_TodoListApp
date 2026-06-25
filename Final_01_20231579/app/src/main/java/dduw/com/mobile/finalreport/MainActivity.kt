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
                //val intent = Intent(this, InfoActivity::class.java)
                //startActivity(intent)
                true
            }
            R.id.off -> {
                finish()
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
        /*
        val todoDao = TodoDao()
        val adapter = TodoAdapter(todoDao.todos.toMutableList())

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        binding.rvTodo.layoutManager = layoutManager
        binding.rvTodo.adapter = adapter
         */
        val formatter = SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault())
        val dateString = formatter.format(Calendar.getInstance().time)

        val adapter = TodoAdapter()
        binding.rvTodo.layoutManager = LinearLayoutManager(this).apply{
            orientation = LinearLayoutManager.VERTICAL
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                val listFlow = todoDao.getTodos()
                listFlow.distinctUntilChanged().collect {
                    todos -> adapter.submitList(todos)
                }
            }
        }


    }
}