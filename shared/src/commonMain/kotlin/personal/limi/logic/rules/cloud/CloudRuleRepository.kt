package personal.limi.logic.rules.cloud

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import personal.limi.utils.datastore.DataStorePreferences

object CloudRuleKeys {
    const val QUERY_STRIPPING_STRIP_LIST = "cloud_query_stripping_strip_list"
    const val QUERY_STRIPPING_ALLOW_LIST = "cloud_query_stripping_allow_list"
    const val QUERY_STRIPPING_LAST_MODIFIED = "cloud_query_stripping_last_modified"
}

object CloudRuleRepository {

    const val QUERY_STRIPPING_URL =
        "https://firefox.settings.services.mozilla.com/v1/buckets/main/collections/query-stripping/records"

    private val client = HttpClient(CIO) {
        expectSuccess = true
    }

    suspend fun syncQueryStripping() {
        val body = client.get(QUERY_STRIPPING_URL).bodyAsText()
        val data = parseQueryStrippingRecords(body)
        DataStorePreferences.putStringSet(CloudRuleKeys.QUERY_STRIPPING_STRIP_LIST, data.stripList)
        DataStorePreferences.putStringSet(CloudRuleKeys.QUERY_STRIPPING_ALLOW_LIST, data.allowList)
        DataStorePreferences.putLong(
            CloudRuleKeys.QUERY_STRIPPING_LAST_MODIFIED, data.lastModified
        )
    }

    fun close() {
        client.close()
    }
}
