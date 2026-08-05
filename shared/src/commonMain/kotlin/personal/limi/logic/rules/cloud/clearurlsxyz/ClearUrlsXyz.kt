package personal.limi.logic.rules.cloud.clearurlsxyz

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.serialization.Serializable
import personal.limi.logic.rules.cloud.cloudRulesJson

object ClearUrlsRuleKeys {
  const val RULES = "cloud_clearurls_rules"
  const val VERSION_HASH = "cloud_clearurls_version_hash"
}

private const val MAX_CLEANING_ITERATIONS = 20

@Serializable
data class ClearUrlsProvider(
  val urlPattern: String = "",
  val completeProvider: Boolean = false,
  val rules: List<String> = emptyList(),
  val rawRules: List<String> = emptyList(),
  val referralMarketing: List<String> = emptyList(),
  val exceptions: List<String> = emptyList(),
  val redirections: List<String> = emptyList(),
) {
  val compiledUrlPattern: Regex? by lazy { compileRegex(urlPattern) }
  val compiledRules: List<Regex> by lazy {
    (rules + referralMarketing).mapNotNull { compileRegex(it) }
  }
  val compiledRawRules: List<Regex> by lazy { rawRules.mapNotNull { compileRegex(it) } }
  val compiledExceptions: List<Regex> by lazy { exceptions.mapNotNull { compileRegex(it) } }
  val compiledRedirections: List<Regex> by lazy { redirections.mapNotNull { compileRegex(it) } }
}

@Serializable
data class ClearUrlsCatalog(val providers: Map<String, ClearUrlsProvider> = emptyMap())

fun parseClearUrls(body: String): ClearUrlsCatalog = cloudRulesJson.decodeFromString(body)

fun decodeClearUrlsRules(raw: String): ClearUrlsCatalog =
  if (raw.isBlank()) ClearUrlsCatalog() else parseClearUrls(raw)

fun processClearUrls(url: Url, catalog: ClearUrlsCatalog): Url {
  val providers = catalog.providers.values.filterNot { it.completeProvider }
  if (providers.isEmpty()) return url

  var current = url.toString()
  var iterations = 0
  while (iterations < MAX_CLEANING_ITERATIONS) {
    val cleaned = cleanOnce(current, providers)
    if (cleaned == current) break
    current = cleaned
    iterations++
  }
  return runCatching { Url(current) }.getOrDefault(url)
}

private fun cleanOnce(urlString: String, providers: List<ClearUrlsProvider>): String {
  var current = urlString
  for (provider in providers) {
    if (!providerMatches(provider, current)) continue

    val redirectTarget = resolveRedirection(provider, current)
    if (redirectTarget != null) return redirectTarget

    current = applyRawRules(current, provider.compiledRawRules)
    current = removeFields(current, provider.compiledRules)
  }
  return current
}

private fun providerMatches(provider: ClearUrlsProvider, urlString: String): Boolean =
  provider.compiledUrlPattern?.containsMatchIn(urlString) == true &&
    provider.compiledExceptions.none {
      it.containsMatchIn(urlString)
    }

private fun resolveRedirection(provider: ClearUrlsProvider, urlString: String): String? {
  for (regex in provider.compiledRedirections) {
    val target = regex.find(urlString)?.groupValues?.getOrNull(1)
    if (!target.isNullOrEmpty()) return decodeRedirectUrl(target)
  }
  return null
}

private fun applyRawRules(urlString: String, rawRules: List<Regex>): String {
  var result = urlString
  for (rule in rawRules) result = runCatching { rule.replace(result, "") }.getOrDefault(result)
  return result
}

private fun removeFields(urlString: String, rules: List<Regex>): String {
  if (rules.isEmpty()) return urlString

  val url = runCatching { Url(urlString) }.getOrNull() ?: return urlString

  val builder = URLBuilder(url)
  var queryChanged = false
  builder.parameters.clear()
  for (entry in url.parameters.entries()) {
    if (rules.any { it.matches(entry.key) }) queryChanged = true
    else builder.parameters.appendAll(entry.key, entry.value)
  }

  var fragmentChanged = false
  val encodedFragment = url.encodedFragment
  if (encodedFragment.isNotEmpty()) {
    val kept =
      encodedFragment.split('&').filter { part ->
        val removed = rules.any { it.matches(part.substringBefore('=')) }
        if (removed) fragmentChanged = true
        !removed
      }
    builder.encodedFragment = kept.joinToString("&")
  }

  if (!queryChanged && !fragmentChanged) return urlString
  return builder.build().toString()
}

private fun decodeRedirectUrl(raw: String): String {
  var result = percentDecode(raw)
  while (true) {
    val decoded = percentDecode(result)
    if (decoded == result) break
    result = decoded
  }
  result = result.trim()

  if (result.startsWith("//")) return "https:$result"

  val scheme = SCHEME_REGEX.find(result)?.value?.dropLast(1)
  return if (scheme != null && '.' !in scheme) result else "http://$result"
}

private val SCHEME_REGEX = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:")

private fun percentDecode(input: String): String {
  val bytes = ArrayList<Byte>(input.length)
  var i = 0
  while (i < input.length) {
    val c = input[i]
    if (c == '%' && i + 2 < input.length) {
      val value = input.substring(i + 1, i + 3).toIntOrNull(16)
      if (value != null) {
        bytes.add(value.toByte())
        i += 3
        continue
      }
    }
    c.toString().encodeToByteArray().forEach { bytes.add(it) }
    i++
  }
  return bytes.toByteArray().decodeToString(throwOnInvalidSequence = false)
}

private fun compileRegex(pattern: String): Regex? =
  runCatching { Regex(pattern, RegexOption.IGNORE_CASE) }.getOrNull()
