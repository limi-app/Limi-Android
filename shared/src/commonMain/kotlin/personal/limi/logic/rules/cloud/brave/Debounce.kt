package personal.limi.logic.rules.cloud.brave

import io.ktor.http.Url
import kotlin.io.encoding.Base64
import kotlinx.serialization.Serializable
import personal.limi.logic.rules.cloud.UrlPattern
import personal.limi.logic.rules.cloud.cloudRulesJson

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
) {
  val compiledInclude: List<UrlPattern> by lazy { include.map { UrlPattern.compile(it) } }
  val compiledExclude: List<UrlPattern> by lazy { exclude.map { UrlPattern.compile(it) } }
}

fun parseDebounce(body: String): List<DebounceRule> = cloudRulesJson.decodeFromString(body)

fun decodeDebounceRules(raw: String): List<DebounceRule> =
  if (raw.isBlank()) emptyList() else parseDebounce(raw)

fun processDebounce(url: Url, rules: List<DebounceRule>): Url? {
  for (rule in rules) {
    if (!ruleMatches(url, rule.compiledInclude, rule.compiledExclude)) continue
    val target = resolveDebounceTarget(url, rule) ?: continue
    return target
  }
  return null
}

private fun resolveDebounceTarget(url: Url, rule: DebounceRule): Url? {
  val action = rule.action ?: return null
  return try {
    when (action) {
      "redirect",
      "base64,redirect" -> {
        val param = rule.param ?: return null
        val raw = url.parameters[param] ?: return null
        if (raw.isBlank()) return null
        val decoded = if (action.startsWith("base64")) decodeBase64Url(raw) else raw
        val target = decoded ?: return null
        buildTargetUrl(target)
      }
      "regex-path",
      "regex-path-template" -> {
        val regex = rule.param ?: return null
        val match = Regex(regex).find(url.encodedPath) ?: return null
        val captures = match.groupValues.drop(1)
        if (captures.isEmpty()) return null
        val raw =
          if (action == "regex-path") {
            val scheme = rule.prepend_scheme ?: "https"
            "$scheme://${captures.joinToString("/")}"
          } else {
            val template = rule.redirect_url_template ?: return null
            var result = template
            captures.forEachIndexed { index, value ->
              result = result.replace("$${index + 1}", value)
            }
            result
          }
        val withQuery =
          if (url.encodedQuery.isNotEmpty() && "?" !in raw) {
            "$raw?${url.encodedQuery}"
          } else raw
        buildTargetUrl(withQuery)
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
  val trimmed = raw.trim()
  if (trimmed.isEmpty()) return null
  return try {
    val normalized = trimmed.replace('-', '+').replace('_', '/')
    val paddingNeeded = (4 - normalized.length % 4) % 4
    val padded = if (paddingNeeded > 0) normalized + "=".repeat(paddingNeeded) else normalized
    Base64.decode(padded).decodeToString()
  } catch (_: Exception) {
    null
  }
}
