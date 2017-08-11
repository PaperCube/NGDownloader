package studio.papercube.ngdownloader

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.File
import java.net.URL

class NGSongLocator private constructor() : BeanObject {
    companion object {
        @JvmStatic fun parse(id: Int, rawJson: String): NGSongLocator? = Gson().fromJson<List<NGSongLocator>>(
                rawJson,
                object : TypeToken<List<NGSongLocator>>() {}.type
        ).firstOrNull().apply { if (this != null) songId = id }

        @JvmStatic internal fun empty() = NGSongLocator().apply {
            params = Params()
        }
    }

    @Transient var songId: Int = 0; internal set

    var url: String? = null; internal set
    var description: String? = null; internal set

    @SerializedName("portal_id")
    var portalId: Int? = null; internal set
    @SerializedName("file_id")
    var fileId: Int? = null; internal set
    @SerializedName("project_id")
    var projectId: Int? = null; internal set
    @SerializedName("item_id")
    var itemId: Int? = null; internal set
    @SerializedName("filesize")
    var fileSize: Int? = null; internal set
    var params: Params? = null; internal set

    internal var songNameFallBack: String? = null;

    fun getURL() = URL(url)

    /**
     * Return a [StreamCopyToFile] object, which represents a task to save the bytes in a stream to a file.
     * @param dir The directory where target file should be saved.
     * @param overrideFileName the name without extension to save the song under forcibly. `null` means override nothing (use original name)
     */
    fun saveToDirectory(dir: File, overrideFileName: String? = null): StreamCopyToFile {
        val fileName = overrideFileName ?: songName
        val targetFile = File(dir.absolutePath + "/$fileName.mp3")
        return StreamCopyToFile(getURL().openStream(), targetFile)
    }

    val songName: String
        get() {
            return songNameFallBack ?: params?.filename?.substringAfterLast('/')?.substringBeforeLast(".mp3")
                    ?: songId.toString()
        }

    override fun equals(other: Any?): Boolean {
        return if (other is NGSongLocator) {
            other.url == url
        } else false
    }

    override fun hashCode(): Int {
        var result = url?.hashCode() ?: 0
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (portalId ?: 0)
        result = 31 * result + (fileId ?: 0)
        result = 31 * result + (projectId ?: 0)
        result = 31 * result + (itemId ?: 0)
        result = 31 * result + (fileSize ?: 0)
        result = 31 * result + (params?.hashCode() ?: 0)
        return result
    }

    class Params : BeanObject {
        var filename: String? = null; internal set
        var name: String? = null; internal set
        var length: Int? = null; internal set
        var loop: Int? = -1; internal set
        var artist: String? = null; internal set
        var icon: String? = null; internal set
    }
}