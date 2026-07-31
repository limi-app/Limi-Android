package personal.limi.logic.rules.cloud.brave

import io.ktor.http.Url
import kotlinx.serialization.json.Json

object CleanUrlsRuleKeys {
    const val RULES = "cloud_brave_clean_urls_rules"
    const val VERSION_HASH = "cloud_brave_clean_urls_version_hash"
}

fun parseCleanUrls(body: String): List<BraveQueryRule> =
    Json { ignoreUnknownKeys = true }.decodeFromString(body)

fun decodeCleanUrlsRules(raw: String): List<BraveQueryRule> =
    if (raw.isBlank()) emptyList() else parseCleanUrls(raw)

fun processCleanUrls(url: Url, rules: List<BraveQueryRule>): Url = processQueryRules(url, rules)
