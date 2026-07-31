package personal.limi.logic.rules.cloud.brave

import io.ktor.http.Url
import kotlinx.serialization.json.Json

object QueryFilterRuleKeys {
    const val RULES = "cloud_brave_query_filter_rules"
    const val VERSION_HASH = "cloud_brave_query_filter_version_hash"
}

fun parseQueryFilter(body: String): List<BraveQueryRule> =
    Json { ignoreUnknownKeys = true }.decodeFromString(body)

fun decodeQueryFilterRules(raw: String): List<BraveQueryRule> =
    if (raw.isBlank()) emptyList() else parseQueryFilter(raw)

fun processQueryFilter(url: Url, rules: List<BraveQueryRule>): Url = processQueryRules(url, rules)
