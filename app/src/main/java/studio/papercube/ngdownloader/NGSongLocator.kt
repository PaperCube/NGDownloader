package studio.papercube.ngdownloader

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.net.URL

class NGSongLocator private constructor() : Serializable {
    companion object {
        @JvmStatic fun parse(rawJson: String): NGSongLocator? = Gson().fromJson<List<NGSongLocator>>(
                rawJson,
                object : TypeToken<List<NGSongLocator>>() {}.type
        ).firstOrNull()
    }

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

    class Params : Serializable {
        var filename: String? = null; internal set
        var name: String? = null; internal set
        var length: Int? = null; internal set
        var loop: Int? = -1; internal set
        var artist: String? = null; internal set
        var icon: String? = null; internal set
    }
}