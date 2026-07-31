package personal.limi.logic.rules.cloud.brave

import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.serialization.Serializable
import personal.limi.logic.rules.cloud.UrlPattern

@Serializable
data class BraveQueryRule(
    val include: List<String> = emptyList(),
    val exclude: List<String> = emptyList(),
    val params: List<String> = emptyList(),
) {
    val compiledInclude: List<UrlPattern> by lazy { include.map { UrlPattern.compile(it) } }
    val compiledExclude: List<UrlPattern> by lazy { exclude.map { UrlPattern.compile(it) } }
}

fun ruleMatches(url: Url, include: List<UrlPattern>, exclude: List<UrlPattern>): Boolean =
    include.any { it.matches(url) } && exclude.none { it.matches(url) }

fun processQueryRules(url: Url, rules: List<BraveQueryRule>): Url {
    val paramsToRemove = mutableSetOf<String>()
    for (rule in rules) {
        if (ruleMatches(url, rule.compiledInclude, rule.compiledExclude)) {
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
