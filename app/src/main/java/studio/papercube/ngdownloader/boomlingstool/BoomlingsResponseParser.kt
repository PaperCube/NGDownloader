package studio.papercube.ngdownloader.boomlingstool

import okhttp3.Response
import studio.papercube.ngdownloader.NGSongLocator
import java.net.URLDecoder

data class DailyLevel(val levelSerial: Int, val levelId: Int)
class DailyParser : ResponseParser<DailyLevel>() {
    override fun parse(inputObj: Response): DailyLevel {
        return parse(tryGetResponseString(inputObj))
    }

    /**
     * @throws RuntimeException
     */
    fun parse(string: String): DailyLevel {
        val split = string.split(delimiters = '|')
        return DailyLevel(split[0].toInt(), split[1].toInt())
    }
}

class SongInfoParser(private val id: Int) : ResponseParser<NGSongLocator>() {
    override fun parse(inputObj: Response): NGSongLocator {
        return parse(tryGetResponseString(inputObj))
    }

    /**
     * @throws IllegalStateException when response string is -1,
     * which indicates the song does not exist or something else went wrong (such as bad query string)
     */
    fun parse(response: String): NGSongLocator {
        if (response == "-1") throw invalidResponse()
        val map = response.toTable("~|~")
        return NGSongLocator.empty().also { nsl ->
            nsl.songId = id
            nsl.itemId = id
            nsl.songNameFallBack = map[2]
            nsl.fileSize = map[5]?.toIntOrNull()?.let { it * 1048576 }
            nsl.url = URLDecoder.decode(map[10], "UTF-8")
            nsl.params?.let { params ->
                params.artist = map[4]
            }
        }
    }
}

class SongIdExtractor : ResponseParser<Int>() {
    override fun parse(inputObj: Response): Int {
        return parse(tryGetResponseString(inputObj))
    }

    fun parse(response: String): Int {
        if (response == "-1") throw invalidResponse()
        return response.substringAfterLast(":35:")
                .substringBefore(":")
                .toInt()
    }

}

private fun String.toTable(delimiter: String): Map<Int, String> {
    val map: LinkedHashMap<Int, String> = LinkedHashMap()
    val iterator = split(delimiters = delimiter).listIterator()
    while (iterator.hasNext()) {
        val id = iterator.next().toIntOrNull() ?: continue
        val string = if (iterator.hasNext()) iterator.next() else break
        map.put(id, string)
    }
    return map
}

private fun invalidResponse(msg: String = "-1") = IllegalStateException("Invalid Response: $msg")