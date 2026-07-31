package personal.limi.logic.rules.cloud.brave

import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.serialization.Serializable
import personal.limi.logic.rules.cloud.matchesPattern

@Serializable
data class BraveQueryRule(
    val include: List<String> = emptyList(),
    val exclude: List<String> = emptyList(),
    val params: List<String> = emptyList(),
)

fun ruleMatches(url: Url, include: List<String>, exclude: List<String>): Boolean =
    include.any { matchesPattern(url, it) } && exclude.none { matchesPattern(url, it) }

fun processQueryRules(url: Url, rules: List<BraveQueryRule>): Url {
    val paramsToRemove = mutableSetOf<String>()
    for (rule in rules) {
        if (ruleMatches(url, rule.include, rule.exclude)) {
            paramsToRemove += rule.params.map { it.lowercase() }
        }
    }
    if (paramsToRemove.isEmpty()) return url

    val filteredParameters = Parameters.build {
        url.parameters.forEach { key, values ->
            if (key.lowercase() !in paramsToRemove) appendAll(key, values)
        }
    }

    return URLBuilder(url).apply {
        parameters.clear()
        parameters.appendAll(filteredParameters)
    }.build()
}
