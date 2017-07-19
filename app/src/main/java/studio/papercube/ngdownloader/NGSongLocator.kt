package studio.papercube.ngdownloader

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class NGSongLocator private constructor() : BeanObject {
    companion object {
        @JvmStatic fun parse(id: Int, rawJson: String): NGSongLocator? = Gson().fromJson<List<NGSongLocator>>(
                rawJson,
                object : TypeToken<List<NGSongLocator>>() {}.type
        ).firstOrNull().apply { if (this != null) songId = id }
    }

    @Transient var songId: Int = 0; private set

    var url: String? = null; private set
    var description: String? = null; private set

    @SerializedName("portal_id")
    var portalId: Int? = null; private set
    @SerializedName("file_id")
    var fileId: Int? = null; private set
    @SerializedName("project_id")
    var projectId: Int? = null; private set
    @SerializedName("item_id")
    var itemId: Int? = null; private set
    @SerializedName("filesize")
    var fileSize: Int? = null; private set
    var params: Params? = null; private set

    fun getURL() = URL(url)

    fun saveToDirectory(dir: File): StreamCopyToFile {
        val fileName = params?.filename?.substringAfterLast('/') ?: (songId.toString() + ".mp3")
        val targetFile = File(dir.absolutePath + "/$fileName")
        return StreamCopyToFile(getURL().openStream(), targetFile)
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