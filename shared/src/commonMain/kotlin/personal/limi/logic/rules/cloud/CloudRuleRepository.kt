package personal.limi.logic.rules.cloud

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import personal.limi.logic.rules.cloud.brave.BraveQueryRule
import personal.limi.logic.rules.cloud.brave.CleanUrlsRuleKeys
import personal.limi.logic.rules.cloud.brave.DebounceRuleKeys
import personal.limi.logic.rules.cloud.brave.QueryFilterRuleKeys
import personal.limi.logic.rules.cloud.brave.parseCleanUrls
import personal.limi.logic.rules.cloud.brave.parseDebounce
import personal.limi.logic.rules.cloud.brave.parseQueryFilter
import personal.limi.logic.rules.cloud.clearurlsxyz.ClearUrlsRuleKeys
import personal.limi.logic.rules.cloud.clearurlsxyz.parseClearUrls
import personal.limi.logic.rules.cloud.firefox.FirefoxRuleKeys
import personal.limi.logic.rules.cloud.firefox.parseQueryStrippingRecords
import personal.limi.utils.datastore.DataStorePreferences

object CloudRuleRepository {

    private const val FIREFOX_QUERY_STRIPPING_URL =
        "https://firefox.settings.services.mozilla.com/v1/buckets/main/collections/query-stripping/records"

    private const val BRAVE_CLEAN_URLS_URL =
        "https://raw.githubusercontent.com/brave/adblock-lists/master/brave-lists/clean-urls.json"

    private const val BRAVE_DEBOUNCE_URL =
        "https://raw.githubusercontent.com/brave/adblock-lists/master/brave-lists/debounce.json"

    private const val BRAVE_QUERY_FILTER_URL =
        "https://raw.githubusercontent.com/brave/adblock-lists/master/brave-lists/query-filter.json"

    private const val CLEAR_URLS_DATA_URL = "https://rules1.clearurls.xyz/data.minify.json"

    private const val CLEAR_URLS_HASH_URL = "https://rules1.clearurls.xyz/rules.minify.hash"

    private val client = HttpClient(CIO) {
        expectSuccess = true
    }

    suspend fun syncFirefoxQueryStripping() {
        val body = client.get(FIREFOX_QUERY_STRIPPING_URL).bodyAsText()
        val data = parseQueryStrippingRecords(body)
        DataStorePreferences.putStringSet(FirefoxRuleKeys.QUERY_STRIPPING_STRIP_LIST, data.stripList)
        DataStorePreferences.putStringSet(FirefoxRuleKeys.QUERY_STRIPPING_ALLOW_LIST, data.allowList)
        DataStorePreferences.putLong(FirefoxRuleKeys.QUERY_STRIPPING_LAST_MODIFIED, data.lastModified)
    }

    suspend fun syncCleanUrls() = syncQueryRules(
        url = BRAVE_CLEAN_URLS_URL,
        rulesKey = CleanUrlsRuleKeys.RULES,
        versionKey = CleanUrlsRuleKeys.VERSION_HASH,
        parser = ::parseCleanUrls,
    )

    suspend fun syncQueryFilter() = syncQueryRules(
        url = BRAVE_QUERY_FILTER_URL,
        rulesKey = QueryFilterRuleKeys.RULES,
        versionKey = QueryFilterRuleKeys.VERSION_HASH,
        parser = ::parseQueryFilter,
    )

    suspend fun syncDebounce() {
        val body = client.get(BRAVE_DEBOUNCE_URL).bodyAsText()
        val rules = parseDebounce(body)
        DataStorePreferences.putString(DebounceRuleKeys.RULES, cloudRulesJson.encodeToString(rules))
        DataStorePreferences.putString(DebounceRuleKeys.VERSION_HASH, contentHash(body))
    }

    suspend fun syncClearUrls() {
        val data = client.get(CLEAR_URLS_DATA_URL).bodyAsText()
        val remoteHash = client.get(CLEAR_URLS_HASH_URL).bodyAsText().trim().lowercase()
        check(contentHash(data) == remoteHash) { "ClearURLs hash mismatch" }
        val catalog = parseClearUrls(data)
        DataStorePreferences.putString(ClearUrlsRuleKeys.RULES, cloudRulesJson.encodeToString(catalog))
        DataStorePreferences.putString(ClearUrlsRuleKeys.VERSION_HASH, remoteHash)
    }

    private suspend fun syncQueryRules(
        url: String,
        rulesKey: String,
        versionKey: String,
        parser: (String) -> List<BraveQueryRule>,
    ) {
        val body = client.get(url).bodyAsText()
        val rules = parser(body)
        DataStorePreferences.putString(rulesKey, cloudRulesJson.encodeToString(rules))
        DataStorePreferences.putString(versionKey, contentHash(body))
    }

    fun close() {
        client.close()
    }
}
