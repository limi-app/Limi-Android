package personal.limi.logic.rules.cloud.firefox

import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.serialization.Serializable
import personal.limi.logic.rules.cloud.cloudRulesJson

object FirefoxRuleKeys {
  const val QUERY_STRIPPING_STRIP_LIST = "cloud_query_stripping_strip_list"
  const val QUERY_STRIPPING_ALLOW_LIST = "cloud_query_stripping_allow_list"
  const val QUERY_STRIPPING_LAST_MODIFIED = "cloud_query_stripping_last_modified"
}

@Serializable
data class QueryStrippingRecord(
  val stripList: List<String> = emptyList(),
  val allowList: List<String> = emptyList(),
  val last_modified: Long = 0L,
)

@Serializable data class QueryStrippingResponse(val data: List<QueryStrippingRecord> = emptyList())

data class QueryStrippingData(
  val stripList: Set<String>,
  val allowList: Set<String>,
  val lastModified: Long,
)

fun parseQueryStrippingRecords(body: String): QueryStrippingData {
  val response = cloudRulesJson.decodeFromString<QueryStrippingResponse>(body)
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

  return URLBuilder(url)
    .apply {
      parameters.clear()
      parameters.appendAll(filteredParameters)
    }
    .build()
}
