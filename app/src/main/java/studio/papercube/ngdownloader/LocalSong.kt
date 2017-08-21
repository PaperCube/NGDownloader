package studio.papercube.ngdownloader

import android.content.Context
import com.google.gson.Gson
import java.io.File

open class LocalSong {
    lateinit var name: String private set
    lateinit var file: File private set
    lateinit var fileName: String private set
    lateinit var artist: String private set
    lateinit var id: String private set
    var size: Long = -1; private set
    var length: Int = -1; private set

    fun delete(): Boolean = file.delete()

    object Loader {
        @JvmStatic
        fun loadFrom(context: Context, file: File): LocalSong {
            val index = LocalSongIndexes.getDefault().findByFile(file)
            return LocalSong().apply localSong@ {
                name = index?.songName ?: file.name
                id = index?.id?.toString() ?: "?"
                this@localSong.file = file
                fileName = file.name
                artist = index?.artist ?: context.getText(R.string.text_unknown_artist).toString()
                size = file.length()
            }
        }

        @JvmStatic
        fun loadSongs(context: Context): List<LocalSong> {
            return getSongDirectory(context).listFiles()
                    .plus(context.getExternalFilesDir(null).listFiles())
                    .toList()
                    .filter { it.extension == "mp3" && it.isFile }
                    .map { loadFrom(context, it) }
        }
    }

    companion object {
        @JvmStatic
        fun getSongDirectory(context: Context): File {
            return File("%s/%s".format(context.getExternalFilesDir(null), "songs")).apply {
                mkdirs()
            }
        }

        @JvmStatic
        fun getSongIndexFile(context: Context): File {
            return File("%s/%s".format(context.getExternalFilesDir(null), "index.json"))
        }
    }
}

class LocalSongIndexes(val file: File) {
    companion object {
        private lateinit var defaultIndex: LocalSongIndexes
        private var defaultIndexIsInitialized = false

        /**
         * Make default index usable.
         */
        fun initDefault(context: Context): LocalSongIndexes {
            if (defaultIndexIsInitialized) return defaultIndex
            defaultIndex = LocalSongIndexes(LocalSong.getSongIndexFile(context))
            defaultIndex.load()
            defaultIndexIsInitialized = true
            return defaultIndex
        }

        /**
         * Before using, you must call [initDefault] first.
         * @throws UninitializedPropertyAccessException if this method is called before [initDefault] is called
         */
        fun getDefault() = defaultIndex
    }

    private var entries_: LocalSongIndexEntries? = null
    val entries: LocalSongIndexEntries get() = entries_ ?: let { load(); entries_!! }

    fun load() = apply {
        if (!file.exists()) entries_ = LocalSongIndexEntries()
        else entries_ = Gson().fromJson<LocalSongIndexEntries>(file.bufferedReader(), LocalSongIndexEntries::class.java)
        if (entries_ == null) entries_ = LocalSongIndexEntries()
    }

    fun save() = apply {
        ensureFileExistence()
        val writer = file.bufferedWriter()
        Gson().toJson(entries, writer)
        writer.close()
    }

    fun add(song: NGSongLocator, localFile: File) = apply {
        val newIndex = LocalSongIndex()
        with(newIndex) {
            localPath = localFile.absolutePath
            id = song.songId
            artist = song.params?.artist
            songName = song.songName
            length = song.params?.length
        }
        entries.getSafeIndexes().add(newIndex)
    }

    fun remove(songIndexElement: LocalSongIndex) = apply {
        entries.getSafeIndexes().remove(songIndexElement)
    }

    fun findByFile(file: File): LocalSongIndex? {
        return entries.getSafeIndexes().firstOrNull { File(it.localPath) == file }
    }

    private fun ensureFileExistence() {
        file.parentFile.mkdirs()
        if (!file.exists()) file.createNewFile()
    }

    class LocalSongIndexEntries : BeanObject {
        internal var indexes: MutableList<LocalSongIndex>? = ArrayList()

        fun trim() {
            indexes = indexes?.filter { File(it.localPath).exists() }?.toMutableList() ?: ArrayList()
        }

        fun getSafeIndexes(): MutableList<LocalSongIndex> {
            val i = indexes ?: ArrayList()
            indexes = i
            return i
        }
    }

    class LocalSongIndex : BeanObject {
        var id: Int = 0
        var localPath: String? = null
        var artist: String? = null
        var songName: String? = null
        var length: Int? = 0
    }
}