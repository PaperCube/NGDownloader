package studio.papercube.ngdownloader

import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import studio.papercube.ngdownloader.boomlingstool.BoomlingsTool
import studio.papercube.ngdownloader.boomlingstool.SongInfoParser
import java.util.concurrent.CancellationException

private val resolversList: MutableList<NGSongResolver> = ArrayList()

abstract class NGSongResolver {
    companion object {
        fun all(): List<NGSongResolver> = resolversList

        fun fromBoolingsDatabase() = boomlingsResolveSolution
        fun fromHtml() = htmlResolveSolution
        fun tryAll() = tryEverySolutionOnce
    }

    /**
     * @throws CancellationException if current thread is interrupted
     */
    abstract fun resolve(id: Int): NGSongLocator

    protected fun throwIfCancelled() {
        if (Thread.currentThread().isInterrupted) throw CancellationException()
    }

    class ParseFailure(msg: String, cause: Throwable? = null) : RuntimeException(msg, cause)
    class SongNotFoundException(songId: Int, cause: Throwable? = null) : RuntimeException(
            "The given song with song id $songId cannot be found.",
            cause
    )
}

abstract class RegisteredNGSongResolver : NGSongResolver() {
    init {
        @Suppress("LeakingThis")
        resolversList.add(this)
    }
}


private val boomlingsResolveSolution = object : RegisteredNGSongResolver() {
    override fun resolve(id: Int): NGSongLocator {
        try {
            return BoomlingsTool.getSongResponse(sharedOkHttpClient, id)
                    .let { SongInfoParser(id).parse(it) }
        } catch (e: IllegalStateException) {
            throw SongNotFoundException(id, e)
        }
    }
}

private val htmlResolveSolution = object : RegisteredNGSongResolver() {
    override fun resolve(id: Int): NGSongLocator {
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

        throwIfCancelled()

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
        } catch (e: IndexOutOfBoundsException) {
            throw SongNotFoundException(id)
        }
    }
}

private val tryEverySolutionOnce = object : NGSongResolver() {
    override fun resolve(id: Int): NGSongLocator {
        var lastException: Exception? = null
        for (solution in NGSongResolver.all()) {
            throwIfCancelled()
            try {
                return solution.resolve(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastException = e
            }
        }

        lastException?.let { throw it } ?: throw IllegalStateException("Try-all failed but no exception thrown")
    }
}