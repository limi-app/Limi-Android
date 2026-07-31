package personal.limi.logic.rules.cloud

import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class QueryStrippingRecord(
    val stripList: List<String> = emptyList(),
    val allowList: List<String> = emptyList(),
    val last_modified: Long = 0L,
)

@Serializable
data class QueryStrippingResponse(
    val data: List<QueryStrippingRecord> = emptyList(),
)

data class QueryStrippingData(
    val stripList: Set<String>,
    val allowList: Set<String>,
    val lastModified: Long,
)

fun parseQueryStrippingRecords(body: String): QueryStrippingData {
    val response = Json { ignoreUnknownKeys = true }.decodeFromString<QueryStrippingResponse>(body)
    return QueryStrippingData(
        stripList = response.data.flatMap { it.stripList }.map { it.lowercase() }.toSet(),
        allowList = response.data.flatMap { it.allowList }.map { it.lowercase() }.toSet(),
        lastModified = response.data.maxOfOrNull { it.last_modified } ?: 0L,
    )
}

fun processQueryStripping(url: Url, stripList: Set<String>, allowList: Set<String>): Url {
    if (url.host.lowercase() in allowList) return url
    val filteredParameters = Parameters.build {
        url.parameters.forEach { key, values ->
            if (key.lowercase() !in stripList) appendAll(key, values)
        }
    }

    return URLBuilder(url).apply {
        parameters.clear()
        parameters.appendAll(filteredParameters)
    }.build()
}
