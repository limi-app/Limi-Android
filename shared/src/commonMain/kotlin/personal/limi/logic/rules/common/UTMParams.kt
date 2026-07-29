package personal.limi.logic.rules.common

import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.Url

fun processUTMParams(url: Url): Url {
    val utmParams = listOf("utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content")
    val filteredParameters = Parameters.build {
        url.parameters.forEach { key, values -> if (key !in utmParams) appendAll(key, values) }
    }

    return URLBuilder(url).apply {
        parameters.clear()
        parameters.appendAll(filteredParameters)
    }.build()
}