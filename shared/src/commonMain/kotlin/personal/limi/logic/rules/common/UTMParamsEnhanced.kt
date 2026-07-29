package personal.limi.logic.rules.common

import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.Url

fun processUTMParamsEnhanced(url: Url): Url {
    val filteredParameters = Parameters.build {
        url.parameters.forEach { key, values -> if (!key.startsWith("utm_")) appendAll(key, values) }
    }

    return URLBuilder(url).apply {
        parameters.clear()
        parameters.appendAll(filteredParameters)
    }.build()
}