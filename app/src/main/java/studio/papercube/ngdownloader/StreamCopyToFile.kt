package studio.papercube.ngdownloader

import android.os.Handler
import android.os.Message
import java.io.*
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import kotlin.concurrent.thread

open class StreamCopyToFile(input: InputStream,
                            val file: File,
                            bufferSize: Int = 16384) : Closeable {
    @Volatile var writtenBytes = 0; private set

    @Volatile var isClosed = false; private set

    private val inputStream = input.buffered(bufferSize)

    private val outputStream: BufferedOutputStream

    init {
        file.parentFile.mkdirs()
        file.createNewFile()
        outputStream = FileOutputStream(file).buffered(bufferSize)
    }

    @Deprecated("Bad design", ReplaceWith("start()"))
    open fun copy(length: Int) {
        if (isClosed) return
        for (i in 0..length - 1) {
            val char = inputStream.read()
            if (char == -1) {
                close()
                break
            } else {
                outputStream.write(char)
                writtenBytes++
            }
        }
    }


    open fun start() {
        if (isClosed) throw IllegalStateException("Object closed")
        while (true) {
            val char = inputStream.read()
            if (char == -1) {
                close()
                break
            } else {
                outputStream.write(char)
                writtenBytes++
            }
        }
    }

    fun watch(refreshRate: Int = 30, action: StreamCopyToFile.() -> Unit) {
        val interval = when {
            refreshRate > 500 -> 2L
            refreshRate < 0 -> return
            else -> 1000L / refreshRate
        }

        while (true) {
            if (Thread.currentThread().isInterrupted || isClosed) return
            action()
            Thread.sleep(interval)
        }
    }

    fun watchHandlerAsync(handler: Handler, executor: ExecutorService, interval: Long = 500) =
            executor.submit {
                while (true) {
                    if (Thread.currentThread().isInterrupted) return@submit
                    handler.sendEmptyMessage(0)
                    Thread.sleep(interval)
                }
            }


    override fun close() {
        inputStream.close()
        outputStream.close()
        isClosed = true
    }
}
