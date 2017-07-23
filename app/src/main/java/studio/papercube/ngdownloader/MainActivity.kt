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
            textDownloadProgress.visibility = if (value) VISIBLE else GONE
        }

    private val downloadProgressUpdateHandler = DownloadProgressUpdateHandler { obj, what ->
        if (obj !is DownloadProgress) return@DownloadProgressUpdateHandler
        if (what != 0) Log.w(LOG_TAG_MAIN, "Download Progress Handler expected what 0, but found $what")
        val (copy, song) = obj
        if (copy.isClosed) return@DownloadProgressUpdateHandler
        val fileSize = song.fileSize ?: -1
        val fileSizeString: String = fileSize.toAppropriateMemoryUnit()
        if (fileSize < 2) {
            progressBar.isIndeterminate = true
        } else {
            progressBar.isIndeterminate = false
            progressBar.max = fileSize
            progressBar.progress = copy.writtenBytes
        }

        R.id.visible
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
    fun actionResolve(v: View){
        textResultOfResolve.text = EMPTY_STRING
        resolveAsync()
    }

    private fun resolveAsync() {
        sharedExecutor.submit {
            doWithUiLocked {
                var message: CharSequence = EMPTY_STRING
                try {
                    resolve().let {
                        message = it.getFormattedDescription()
                    }

                } catch (e: Exception) {
                    val msg = getText(R.string.error_song_parse_failure)
                    message = SpannableStringBuilder()
                            .lineAppended("$msg: ${e.printStackTraceToString()}",
                                    ForegroundColorSpan(Color.RED))
                    //TODO simplify error output
                    Log.e(LOG_TAG_MAIN, "Failed to resolve song\n" +
                            e.printStackTraceToString())
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
        textResultOfResolve.text = EMPTY_STRING
        downloadAsync()
    }

    private fun downloadAsync() {
        sharedExecutor.submit {
            try {
                doWithUiLocked {
                    val song = currentSong
                            ?.takeIf { it.songId == fetchSongIdFromEditText() }
                            ?: resolve()

                    currentSong = song

                    runOnUiThread { textResultOfResolve.text = song.getFormattedDescription() }

                    val copy = song.saveToDirectory(getExternalFilesDir(null))
                    LooperThread(500, "Progress notifier") {
                        downloadProgressUpdateHandler.sendMessage(Message().apply {
                            obj = DownloadProgress(copy, song)
                        })

                        return@LooperThread !copy.isClosed
                    }.start()

                    copy.start()

                    runOnUiThread {
                        Toast.makeText(this, "${getText(R.string.notice_saved_song_to)}" +
                                " ${copy.file.absolutePath}", Toast.LENGTH_LONG)
                                .show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    val msg = getText(R.string.error_song_download_failure).toString()
                    textResultOfResolve.text = SpannableStringBuilder()
                            .lineAppended("$msg: ${e.printStackTraceToString()}", ForegroundColorSpan(Color.RED))
                    //TODO simplify error output
                    topLayout.createSnackBar("$msg: $e")
                    Log.e(LOG_TAG_MAIN, "Failed to download song")
                    Log.e(LOG_TAG_MAIN, e.printStackTraceToString())
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

    private fun NGSongLocator.getFormattedDescription(isDetailed: Boolean = true) = StringBuilder().apply {
        if (isDetailed) {
            appendln("URL: $url")
        }
        appendln("${getText(R.string.song_param_file_size)}: ${fileSize?.toAppropriateMemoryUnit()}")
        if (isDetailed)
            appendln("${getText(R.string.song_param_song_length)}: ${params?.length}")
        appendln("${getText(R.string.song_param_artist)}: ${params?.artist}")
    }
}

fun Int.toAppropriateMemoryUnit() = toLong().toAppropriateMemoryUnit()
fun Long.toAppropriateMemoryUnit(): String {
    if (this < 0) return "?"
    val memoryUnits = arrayOf("B", "KB", "MB", "GB", "TB")
    var temp = this.toDouble()
    var power = 0
    while (temp >= 1024) {
        power++
        temp /= 1024
    }

    return "${"%.2f".format(temp)} ${memoryUnits[power]}"
}
