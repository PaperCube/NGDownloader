package studio.papercube.ngdownloader

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import studio.papercube.ngdownloader.widgets.createProgressDialog
import studio.papercube.ngdownloader.widgets.lineAppended

class MainActivity : AppCompatActivity() {
    lateinit var editTextSongId: EditText
    lateinit var buttonResolve: Button
    lateinit var textResultOfResolve: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        editTextSongId = findViewById(R.id.input_field_song_id) as EditText
        buttonResolve = findViewById(R.id.button_resolve) as Button
        textResultOfResolve = findViewById(R.id.text_resolve_result) as TextView
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    fun actionResolve(v: View) {
        createProgressDialog("Resolving", false) {
            var result = ""
            try {
                val songId = editTextSongId.text.toString().toIntOrNull() ?: 0
                val song = NGSongResolver.resolve(songId)
                result = song.url.toString()
            } catch (e: Exception) {
                result = SpannableStringBuilder()
                        .lineAppended("Cannot resolve:$e", ForegroundColorSpan(Color.RED))
                        .toString()
            }

            runOnUiThread { textResultOfResolve.text = result }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }

    }
}
