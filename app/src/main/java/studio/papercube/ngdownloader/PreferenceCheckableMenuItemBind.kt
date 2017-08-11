package studio.papercube.ngdownloader

import android.content.SharedPreferences
import android.view.MenuItem
import java.util.concurrent.ConcurrentHashMap

open class PreferenceCheckableMenuItemBind(private val preference: SharedPreferences) {
    private val binds: MutableMap<String, Pair<MenuItem, Boolean>> = ConcurrentHashMap()

    open fun bind(key: String, menuItem: MenuItem, defaultValue: Boolean = false) = apply {
        menuItem.isCheckable = true
        binds.put(key, Pair(menuItem, defaultValue))
    }

    open fun <E : Enum<E>> bind(key: Enum<E>, menuItem: MenuItem, defaultValue: Boolean = false) = apply {
        bind(key.name, menuItem, defaultValue)
    }

    open fun retrieve() = apply {
        for ((keyName, pair) in binds) {
            val (menuItem, defaultValue) = pair
            menuItem.isChecked = preference.getBoolean(keyName, defaultValue)
        }
    }

    open fun store() = apply {
        val preferenceEditor = preference.edit()
        for ((keyName, pair) in binds) {
            val (menuItem, _) = pair
            preferenceEditor.putBoolean(keyName, menuItem.isChecked)
        }
        preferenceEditor.apply()
    }

    open fun update(key: String): Boolean {
        binds[key]?.let { (menuItem, _) ->
            // I really cannot understand why the following statement is necessary.
            // I think it should be processed by the system automatically, but in fact it didn't.
            // If you know how this comes, please tell me / send me an issue on github.
            menuItem.isChecked = !menuItem.isChecked
            preference.edit()
                    .putBoolean(key, menuItem.isChecked)
                    .apply()
        } ?: return false
        return true
    }

    open fun <E : Enum<E>> update(key: Enum<E>) = update(key.name)

    open operator fun get(key: String, defaultValue: Boolean) = preference.getBoolean(key, defaultValue)
    open operator fun <E : Enum<E>> get(key: Enum<E>, defaultValue: Boolean) = get(key.name, defaultValue)
}