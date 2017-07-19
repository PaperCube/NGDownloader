package studio.papercube.ngdownloader

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import studio.papercube.ngdownloader.widgets.createSnackBar
import studio.papercube.ngdownloader.widgets.lineAppended

class MainActivity : AppCompatActivity() {
    lateinit var editTextSongId: EditText
    lateinit var buttonResolve: Button
    lateinit var buttonDownload: Button
    lateinit var textResultOfResolve: TextView
    lateinit var progressBar: ProgressBar
    lateinit var topLayout: CoordinatorLayout
    lateinit var textDownloadProgress: TextView

    private var currentSong: NGSongLocator? = null

    private var isInProgress: Boolean = false
        set(value) {
            buttonResolve.isEnabled = !value
            buttonDownload.isEnabled = !value
            progressBar.isIndeterminate = value
            progressBar.visibility = if (value) VISIBLE else GONE
            textDownloadProgress.visibility = if(value) VISIBLE else GONE
        }

    private val downloadProgressUpdateHandler = DownloadProgressUpdateHandler { obj, what ->
        if (obj !is DownloadProgress) return@DownloadProgressUpdateHandler
        if (what != 0) Log.w(LOG_TAG_MAIN, "Download Progress Handler expected what 0, but found $what")
        val (copy, song) = obj
        if (copy.isClosed) return@DownloadProgressUpdateHandler
        val fileSize = song.fileSize ?: -1
        val fileSizeString:String = fileSize.toAppropriateMemoryUnit()
        if (fileSize < 2) {
            progressBar.isIndeterminate = true
        } else {
            progressBar.isIndeterminate = false
            progressBar.max = fileSize
            progressBar.progress = copy.writtenBytes
        }

        textDownloadProgress.text = "${copy.writtenBytes.toAppropriateMemoryUnit()}/$fileSizeString"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextSongId = findViewById(R.id.input_field_song_id) as EditText
        buttonResolve = findViewById(R.id.button_resolve) as Button
        buttonDownload = findViewById(R.id.button_download) as Button
        textResultOfResolve = findViewById(R.id.text_resolve_result) as TextView
        progressBar = findViewById(R.id.progress_bar) as ProgressBar
        topLayout = findViewById(R.id.top_view) as CoordinatorLayout
        textDownloadProgress = findViewById(R.id.text_download_progress) as TextView

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        progressBar.visibility = GONE

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    @Suppress("UNUSED_PARAMETER")
    fun actionResolve(v: View) = resolveAsync()

    private fun resolveAsync() {
        sharedExecutor.submit {
            doWithUiLocked {
                var message: CharSequence = "未知状态"
                try {
                    resolve().let {
                        message = StringBuilder()
                                .appendln("URL:${it.url}")
                                .appendln("File size:${(it.fileSize?.toAppropriateMemoryUnit()) ?: "?"}")
                    }

                } catch (e: Exception) {
                    message = SpannableStringBuilder()
                            .lineAppended("无法解析 : $e", ForegroundColorSpan(Color.RED))
                    throw e
                } finally {
                    runOnUiThread {
                        textResultOfResolve.text = message
                    }
                }
            }
        }
    }

    private fun resolve(): NGSongLocator {
        val song: NGSongLocator = fetchSongIdFromEditText().let { NGSongResolver.resolve(it) }
        currentSong = song
        return song
    }

    @Suppress("UNUSED_PARAMETER")
    fun actionDownload(v: View) {
        downloadAsync()
    }

    private fun downloadAsync() {
        sharedExecutor.submit {
            try {
                doWithUiLocked {
                    val song = currentSong
                            ?.takeIf { it.songId == fetchSongIdFromEditText() }
                            ?: resolve()

                    val copy = song.saveToDirectory(getExternalFilesDir(null))
                    LooperThread(500,"Progress notifier"){
                        downloadProgressUpdateHandler.sendMessage(Message().apply {
                            obj = DownloadProgress(copy, song)
                        })

                        return@LooperThread !copy.isClosed
                    }.start()

                    copy.start()

                    runOnUiThread {
                        Toast.makeText(this, "Saved song to ${copy.file.absolutePath}", Toast.LENGTH_LONG)
                                .show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    topLayout.createSnackBar("下载失败：$e")
                }
            }
        }
    }

    inline private fun doWithUiLocked(action: () -> Unit) {
        runOnUiThread { isInProgress = true }
        try {
            action()
        } finally {
            runOnUiThread {
                isInProgress = false
            }
        }

    }

    private fun fetchSongIdFromEditText(): Int = editTextSongId.text.toString().trim().toIntOrNull() ?: -1

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        return when (id) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }

    }

    private class DownloadProgressUpdateHandler(private val task: (Any?, Int) -> Unit) : Handler() {
        override fun handleMessage(msg: Message?) {
            msg ?: return
            task(msg.obj, msg.what)
        }
    }

    data class DownloadProgress(val copy: StreamCopyToFile, val song: NGSongLocator)

}

fun Int.toAppropriateMemoryUnit() = toLong().toAppropriateMemoryUnit()
fun Long.toAppropriateMemoryUnit(): String {
    if(this < 0) return "?"
    val memoryUnits = arrayOf("B", "KB", "MB", "GB", "TB")
    var temp = this.toDouble()
    var power = 0
    while (temp >= 1024) {
        power++
        temp /= 1024
    }

    return "${"%.2f".format(temp)} ${memoryUnits[power]}"
}
