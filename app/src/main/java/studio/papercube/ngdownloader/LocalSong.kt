package studio.papercube.ngdownloader

import android.content.Context
import java.io.File

open class LocalSong {
    lateinit var name: String private set
    lateinit var file: File private set
    lateinit var fileName: String private set
    lateinit var artist: String private set
    lateinit var id: String private set
    var size: Long = -1; private set
    var length: Int = -1; private set

    object Loader {
        @JvmStatic
        fun loadFrom(context: Context, file: File): LocalSong {
            return LocalSong().apply localSong@ {
                name = "name"
                id = "000"
                this@localSong.file = file
                fileName = file.name
                artist = "Various Artists"
                size = file.length()
            }
        }

        @JvmStatic
        fun loadSongs(context: Context): List<LocalSong> {
            return context.getExternalFilesDir(null).listFiles()
                    .toList()
                    .filter { it.extension == "mp3" }
                    .map { loadFrom(context, it) }
        }
    }
}