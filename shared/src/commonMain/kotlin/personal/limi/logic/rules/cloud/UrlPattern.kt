package personal.limi.logic.rules.cloud

import io.ktor.http.Url

fun matchesPattern(url: Url, pattern: String): Boolean {
    val schemeSeparator = pattern.indexOf("://")
    if (schemeSeparator < 0) return false
    val schemePattern = pattern.substring(0, schemeSeparator)
    val rest = pattern.substring(schemeSeparator + 3)
    val slashIndex = rest.indexOf('/')
    val hostPattern = if (slashIndex < 0) rest else rest.substring(0, slashIndex)
    val pathPattern = if (slashIndex < 0) "" else rest.substring(slashIndex)

    if (schemePattern != "*" && !url.protocol.name.equals(schemePattern, ignoreCase = true)) return false

    val host = url.host.lowercase()
    val normalizedHostPattern = hostPattern.lowercase()
    when {
        normalizedHostPattern == "*" -> Unit
        normalizedHostPattern.startsWith("*.") -> {
            val suffix = normalizedHostPattern.substring(2)
            if (host != suffix && !host.endsWith(".$suffix")) return false
        }

        else -> if (host != normalizedHostPattern) return false
    }

    if (pathPattern.isNotEmpty() && !globMatches(url.pathWithQuery(), pathPattern)) return false
    return true
}

private fun Url.pathWithQuery(): String =
    if (encodedQuery.isEmpty()) encodedPath else "$encodedPath?$encodedQuery"

private fun globMatches(value: String, pattern: String): Boolean {
    val regex = buildString {
        append('^')
        for (c in pattern) {
            if (c == '*') append(".*") else append(Regex.escape(c.toString()))
        }
        append('$')
    }
    return Regex(regex).matches(value)
}
