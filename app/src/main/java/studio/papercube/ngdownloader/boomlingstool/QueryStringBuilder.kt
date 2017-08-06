package studio.papercube.ngdownloader.boomlingstool

import java.util.*

open class QueryStringBuilder() {
    private val map: LinkedHashMap<String, String> = LinkedHashMap()

    constructor(init: Map<String, String>) : this() {
        map.putAll(init)
    }

    override fun toString() = map
            .entries
            .joinToString("&") { (key, value) ->
                "$key=$value"
            }

    open fun add(key: String, value: Any?) = apply {
        map.put(key, value.toString())
    }
}
