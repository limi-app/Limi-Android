package personal.limi.logic.rules.cloud

import io.ktor.http.Url

class UrlPattern private constructor(
    private val schemePattern: String,
    private val hostPattern: String,
    private val pathRegex: Regex?,
    private val neverMatches: Boolean,
) {
    fun matches(url: Url): Boolean {
        if (neverMatches) return false

        if (schemePattern != "*" && !url.protocol.name.equals(schemePattern, ignoreCase = true)) return false

        val host = url.host.lowercase()
        when {
            hostPattern == "*" -> Unit
            hostPattern.startsWith("*.") -> {
                val suffix = hostPattern.substring(2)
                if (host != suffix && !host.endsWith(".$suffix")) return false
            }

            else -> if (host != hostPattern) return false
        }

        if (pathRegex != null && !pathRegex.matches(url.pathWithQuery())) return false
        return true
    }

    companion object {
        fun compile(pattern: String): UrlPattern {
            val schemeSeparator = pattern.indexOf("://")
            if (schemeSeparator < 0) return UrlPattern("", "", null, neverMatches = true)
            val schemePattern = pattern.substring(0, schemeSeparator)
            val rest = pattern.substring(schemeSeparator + 3)
            val slashIndex = rest.indexOf('/')
            val hostPattern = if (slashIndex < 0) rest else rest.substring(0, slashIndex)
            val pathPattern = if (slashIndex < 0) "" else rest.substring(slashIndex)
            return UrlPattern(
                schemePattern = schemePattern,
                hostPattern = hostPattern.lowercase(),
                pathRegex = if (pathPattern.isEmpty()) null else globToRegex(pathPattern),
                neverMatches = false,
            )
        }

        private fun globToRegex(pattern: String): Regex {
            val regex = buildString {
                append('^')
                for (c in pattern) {
                    if (c == '*') append(".*") else append(Regex.escape(c.toString()))
                }
                append('$')
            }
            return Regex(regex)
        }
    }
}

fun matchesPattern(url: Url, pattern: String): Boolean = UrlPattern.compile(pattern).matches(url)

private fun Url.pathWithQuery(): String =
    if (encodedQuery.isEmpty()) encodedPath else "$encodedPath?$encodedQuery"
