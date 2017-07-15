@file:Suppress("unused")

package studio.papercube.ngdownloader.widgets

import android.text.SpannableStringBuilder
import android.text.Spanned

private typealias SpanBuilder = SpannableStringBuilder

fun SpanBuilder.spanned(what: Any?, start: Int, end: Int, flags: Int) = apply { setSpan(what, start, end, flags) }

fun SpanBuilder.lineAppended(text: CharSequence?, what: Any?, flags: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) = apply {
    val start = length
    val end = start + text.toString().length
    appendln(text.toString())
    setSpan(what, start, end, flags)
}

inline fun SpanBuilder.lineAppendedIf(
        text: CharSequence?,
        what: Any?,
        flags: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        condition: () -> Boolean) = apply {
    if (condition()) lineAppended(text, what, flags)
}
