package studio.papercube.ngdownloader

import java.io.PrintWriter
import java.io.StringWriter

const val LOG_TAG_MAIN = "NGDownloader"
const val LOG_TAG_IGNORED_EXCEPTION = "NGDownloaderExceptions"
const val EMPTY_STRING = "" //seems meaningless for auto-optimization of compiler

fun Throwable.printStackTraceToString(): String {
    val stringWriter = StringWriter()
    printStackTrace(PrintWriter(stringWriter))
    return stringWriter.toString()
}