package studio.papercube.ngdownloader

import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.CancellationException

class NGSongResolver {
    companion object {
        @JvmStatic fun resolve(id: Int): NGSongLocator {
            val document: Document?
            val responseBody = Request.Builder()
                    .url("http://newgrounds.com/audio/listen/$id")
                    .get()
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Accept", "text/html")
                    .addHeader("Accept-Encoding", "charset=utf8")
                    .build()
                    .let {
                        sharedOkHttpClient.newCall(it)
                                .execute()
                    }.body()

            cancelIfInterrupted()

            document = Jsoup.parse(responseBody?.byteStream(), "UTF-8", "newgrounds.com")
            try {
                return document.body()
                        .allElements //here, if select(title) contains Error, then regard it as a non-existing song.
                        .select("script")[7]
                        .data()
                        .substringAfter("new embedController(")
                        .substringBeforeLast("function()")
                        .substringBeforeLast("callback:")
                        .substringBeforeLast(",")
                        .let { it + "}]" }
                        .let { NGSongLocator.parse(id, it) } ?:
                        throw ParseFailure("Failed to find specified html element.")
            } catch (e: ArrayIndexOutOfBoundsException) {
                throw SongNotFoundException(id)
            }
        }

        @JvmStatic private fun cancelIfInterrupted() {
            if (Thread.currentThread().isInterrupted) throw CancellationException()
        }
    }

    class ParseFailure(msg: String, cause: Throwable? = null) : RuntimeException(msg, cause)
    class SongNotFoundException(songId: Int, cause: Throwable? = null) : RuntimeException(
            "The given song with song id $songId cannot be found.",
            cause
    )
}