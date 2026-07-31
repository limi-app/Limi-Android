package personal.limi.logic.rules.cloud.brave

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object DebounceRuleKeys {
    const val RULES = "cloud_brave_debounce_rules"
    const val VERSION_HASH = "cloud_brave_debounce_version_hash"
}

@Serializable
data class DebounceRule(
    val include: List<String> = emptyList(),
    val exclude: List<String> = emptyList(),
    val action: String? = null,
    val param: String? = null,
    val prepend_scheme: String? = null,
    val redirect_url_template: String? = null,
)

fun parseDebounce(body: String): List<DebounceRule> =
    Json { ignoreUnknownKeys = true }.decodeFromString(body)

fun decodeDebounceRules(raw: String): List<DebounceRule> =
    if (raw.isBlank()) emptyList() else parseDebounce(raw)

fun processDebounce(url: Url, rules: List<DebounceRule>): Url? {
    for (rule in rules) {
        if (!ruleMatches(url, rule.include, rule.exclude)) continue
        val target = resolveDebounceTarget(url, rule) ?: continue
        return target
    }
    return null
}

private fun resolveDebounceTarget(url: Url, rule: DebounceRule): Url? {
    val action = rule.action ?: return null
    return try {
        when {
            action == "redirect" || action == "base64,redirect" -> {
                val param = rule.param ?: return null
                val raw = url.parameters[param] ?: return null
                if (raw.isBlank()) return null
                val decoded = if (action.startsWith("base64")) decodeBase64Url(raw) else raw
                val target = decoded ?: return null
                buildTargetUrl(target)
            }

            action == "regex-path" || action == "regex-path-template" -> {
                val regex = rule.param ?: return null
                val match = Regex(regex).find(url.encodedPath) ?: return null
                val captures = match.groupValues.drop(1)
                if (captures.isEmpty()) return null
                if (action == "regex-path") {
                    val scheme = rule.prepend_scheme ?: "https"
                    buildTargetUrl("$scheme://${captures.joinToString("/")}")
                } else {
                    val template = rule.redirect_url_template ?: return null
                    var result = template
                    captures.forEachIndexed { index, value ->
                        result = result.replace("\$${index + 1}", value)
                    }
                    buildTargetUrl(result)
                }
            }

            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

private fun buildTargetUrl(raw: String): Url? {
    val withScheme = if (raw.contains("://")) raw else "https://$raw"
    return try {
        Url(withScheme)
    } catch (_: Exception) {
        null
    }
}

private fun decodeBase64Url(raw: String): String? {
    return try {
        kotlin.io.encoding.Base64.Default.decode(raw).decodeToString()
    } catch (_: Exception) {
        null
    }
}
