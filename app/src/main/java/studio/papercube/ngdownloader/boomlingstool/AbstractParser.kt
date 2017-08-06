package studio.papercube.ngdownloader.boomlingstool

import okhttp3.Response
import java.io.IOException

abstract class AbstractParser<in In, out Out> {
    abstract fun parse(inputObj: In): Out
}

abstract class ResponseParser<out Out> : AbstractParser<Response, Out>() {
    protected fun tryGetResponseString(response: Response): String {
        return response.body()?.string() ?:
                throw IOException("Null response body. Response Code:${response.code()}")
    }
}