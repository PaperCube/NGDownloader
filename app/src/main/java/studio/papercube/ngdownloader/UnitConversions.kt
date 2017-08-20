package studio.papercube.ngdownloader

import android.content.Context

object MemoryUnitConversions {
    @JvmStatic
    fun Int.toAppropriateMemoryUnit() = toLong().toAppropriateMemoryUnit()

    @JvmStatic
    fun Long.toAppropriateMemoryUnit(): String {
        if (this < 0) return "?"
        val memoryUnits = arrayOf("B", "KB", "MB", "GB", "TB")
        var temp = this.toDouble()
        var power = 0
        while (temp >= 1024) {
            power++
            temp /= 1024
        }

        return if (power > 0)
            "${"%.2f".format(temp)} ${memoryUnits[power]}"
        else
            "$this ${memoryUnits[power]}"
    }
}

object TimeConversions {
    data class ShortPeriod(val hours: Int, val minutes: Int, val seconds: Int) {
        companion object {
            @JvmStatic fun ofSeconds(seconds: Int): ShortPeriod {
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val sec = seconds % 3600 % 60
                return ShortPeriod(hours, minutes, sec)
            }
        }
    }


    class ShortPeriodLocalization(ctx: Context,
                                  timeFormatRes: Int) {
        private val formatString = ctx.getText(timeFormatRes).toString()
        fun localize(shortPeriod: ShortPeriod): String {
            val (h, m, s) = shortPeriod
            return formatString.format(h, m, s)
        }
    }
}