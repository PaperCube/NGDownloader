package studio.papercube.ngdownloader.boomlingstool

import okhttp3.*

object BoomlingsTool {
    @JvmStatic
    val secretCode = "Wmfd2893gb7"
    private val mediaTypeUrlEncoded = MediaType.parse("application/x-www-form-urlencoded")
    private val baseURL = "http://www.boomlings.com/database"

    @JvmStatic
    fun getSongResponse(okHttpClient: OkHttpClient, songId: Int): Response {
        return Request.Builder()
                .url("$baseURL/getGJSongInfo.php")
//            .addHeader("Accept","*/*")
                .post(RequestBody.create(
                        mediaTypeUrlEncoded,
                        //"songID=$songId&secret=Wmfd2893gb7"
                        newGDEssentialQueryStringBuilder()
                                .add("songId", songId)
                                .toString()
                ))
                .build()
                .let { okHttpClient.request(it) }
    }

    @JvmStatic
    fun getDailyLevelResponse(okHttpClient: OkHttpClient): Response {
        return Request.Builder()
                .url("$baseURL/getGJDailyLevel.php")
                .post(RequestBody.create(
                        mediaTypeUrlEncoded,
                        //"gameVersion=21&binaryVersion=33&secret=Wmfd2893gb7"
                        newGDEssentialQueryStringBuilder()
                                .toString()
                ))
                .build()
                .let { okHttpClient.request(it) }
    }

    @JvmStatic
    fun getLevelResponse(okHttpClient: OkHttpClient, levelId: Int): Response =
            Request.Builder()
                    .url("$baseURL/downloadGJLevel22.php")
                    .post(RequestBody.create(
                            mediaTypeUrlEncoded,
                            //                            "gameVersion=21&binaryVersion=33&gdw=0&levelID=$levelId&inc=1&extras=0&secret=Wmfd2893gb7"
                            newGDEssentialQueryStringBuilder()
                                    .add("gdw", 0)
                                    .add("levelID", levelId)
                                    .add("inc", 1)
                                    .add("extras", 0)
                                    .toString()
                    ))
                    .build()
                    .let { okHttpClient.request(it) }


    private fun OkHttpClient.request(request: Request) = newCall(request).execute()
    private fun QueryStringBuilder.addSecret() = add("secret", secretCode)
    @JvmStatic
    fun newGDEssentialQueryStringBuilder() = QueryStringBuilder()
            .add("gameVersion", 21)
            .add("binaryVersion", 33)
            .addSecret()

    @JvmStatic
    fun getDaily(okHttpClient: OkHttpClient): DailyLevel {
        return DailyParser().parse(getDailyLevelResponse(okHttpClient))
    }

    @JvmStatic
    fun getSongIdFromLevelId(okHttpClient: OkHttpClient, levelId: Int): Int {
        return SongIdExtractor().parse(getLevelResponse(okHttpClient, levelId))
    }
}
