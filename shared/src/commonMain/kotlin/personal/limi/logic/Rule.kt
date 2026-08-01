package personal.limi.logic

import io.ktor.http.Url
import personal.limi.data.rules.RuleConfig
import personal.limi.logic.rules.bilibili.bilibiliNoParamsTargetHost
import personal.limi.logic.rules.bilibili.bilibiliRedirectTargetHost
import personal.limi.logic.rules.bilibili.processBilibiliNoParamsUrl
import personal.limi.logic.rules.bilibili.processBilibiliRedirectUrl
import personal.limi.logic.rules.cloud.brave.CleanUrlsRuleKeys
import personal.limi.logic.rules.cloud.brave.DebounceRuleKeys
import personal.limi.logic.rules.cloud.brave.QueryFilterRuleKeys
import personal.limi.logic.rules.cloud.brave.decodeCleanUrlsRules
import personal.limi.logic.rules.cloud.brave.decodeDebounceRules
import personal.limi.logic.rules.cloud.brave.decodeQueryFilterRules
import personal.limi.logic.rules.cloud.brave.processCleanUrls
import personal.limi.logic.rules.cloud.brave.processDebounce
import personal.limi.logic.rules.cloud.brave.processQueryFilter
import personal.limi.logic.rules.cloud.clearurlsxyz.ClearUrlsRuleKeys
import personal.limi.logic.rules.cloud.clearurlsxyz.decodeClearUrlsRules
import personal.limi.logic.rules.cloud.clearurlsxyz.processClearUrls
import personal.limi.logic.rules.cloud.firefox.FirefoxRuleKeys
import personal.limi.logic.rules.cloud.firefox.processQueryStripping
import personal.limi.logic.rules.common.processUTMParams
import personal.limi.logic.rules.common.processUTMParamsEnhanced
import personal.limi.utils.datastore.DataStorePreferences

object RuleIds {
    const val BILIBILI = "bilibili_rules"
    const val UTM_PARAMS = "utm_params_rules"
    const val UTM_PARAMS_ENHANCED = "utm_params_enhanced_rules"
    const val FIREFOX_QUERY_STRIPPING = "firefox_query_stripping_rules"
    const val BRAVE_CLEAN_URLS = "brave_clean_urls_rules"
    const val BRAVE_DEBOUNCE = "brave_debounce_rules"
    const val BRAVE_QUERY_FILTER = "brave_query_filter_rules"
    const val CLEAR_URLS = "clear_urls_rules"
}

private val URL_SCHEME_REGEX = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*")
private val SCHEME_PREFIX_REGEX = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:(//)?")

/**
 * 判断链接是否自带协议头
 */
internal fun hasUrlScheme(urlString: String): Boolean {
    val colonIndex = urlString.indexOf(':')
    if (colonIndex <= 0) return false
    val candidate = urlString.substring(0, colonIndex)
    if (!URL_SCHEME_REGEX.matches(candidate)) return false
    // 排除 host:port 误判（如 a.com:8080）：合法 scheme 不含 "."
    if (candidate.contains('.')) return false
    // localhost 是唯一常见的无 "." hostname
    if (candidate.lowercase() == "localhost") return false
    // host:port 误判（如 intranet:8080）：冒号后是纯数字端口时视为 host:port
    val port = urlString.substring(colonIndex + 1)
        .substringBefore('/')
        .substringBefore('?')
        .substringBefore('#')
    if (port.isNotEmpty() && port.all { it.isDigit() }) return false
    return true
}

/**
 * 为不带协议的链接补全 http://，避免被解析为 http://localhost/<path>
 */
internal fun ensureUrlScheme(urlString: String): String =
    if (hasUrlScheme(urlString)) urlString else "http://$urlString"

/**
 * 保持用户链接原样式：原链接不带协议头时，不输出协议头
 */
internal fun applyOriginalSchemeStyle(result: String, hadScheme: Boolean): String =
    if (hadScheme) result else result.replaceFirst(SCHEME_PREFIX_REGEX, "")

suspend fun processUrl(urlString: String): String {

    val ruleConfig = RuleConfig(
        UTMParams = DataStorePreferences.getBoolean(RuleIds.UTM_PARAMS, true),
        UTMParamsEnhanced = DataStorePreferences.getBoolean(RuleIds.UTM_PARAMS_ENHANCED, false),
        bilibili = DataStorePreferences.getBoolean(RuleIds.BILIBILI, true),
        firefoxQueryStripping = DataStorePreferences.getBoolean(RuleIds.FIREFOX_QUERY_STRIPPING, false),
        braveCleanUrls = DataStorePreferences.getBoolean(RuleIds.BRAVE_CLEAN_URLS, false),
        braveDebounce = DataStorePreferences.getBoolean(RuleIds.BRAVE_DEBOUNCE, false),
        braveQueryFilter = DataStorePreferences.getBoolean(RuleIds.BRAVE_QUERY_FILTER, false),
        clearUrls = DataStorePreferences.getBoolean(RuleIds.CLEAR_URLS, false)
    )

    val hadScheme = hasUrlScheme(urlString)
    var finalUrl = Url(ensureUrlScheme(urlString))

    // 第一层：去重定向跳转
    if (ruleConfig.braveDebounce) {
        val rules = decodeDebounceRules(DataStorePreferences.getString(DebounceRuleKeys.RULES, ""))
        if (rules.isNotEmpty()) {
            finalUrl = processDebounce(finalUrl, rules) ?: finalUrl
        }
    }

    // 第二层：特定 Host 匹配处理
    finalUrl = when {
        ruleConfig.bilibili && finalUrl.host.lowercase() in bilibiliRedirectTargetHost -> processBilibiliRedirectUrl(
            finalUrl
        )

        ruleConfig.bilibili && finalUrl.host.lowercase() in bilibiliNoParamsTargetHost -> processBilibiliNoParamsUrl(
            finalUrl
        )

        else -> finalUrl
    }

    // 第三层：通用参数去除处理
    if (ruleConfig.UTMParams) finalUrl = processUTMParams(finalUrl)
    if (ruleConfig.UTMParamsEnhanced) finalUrl = processUTMParamsEnhanced(finalUrl)
    if (ruleConfig.firefoxQueryStripping) {
        val stripList = DataStorePreferences.getStringSet(FirefoxRuleKeys.QUERY_STRIPPING_STRIP_LIST, emptySet())
        if (stripList.isNotEmpty()) {
            val allowList =
                DataStorePreferences.getStringSet(FirefoxRuleKeys.QUERY_STRIPPING_ALLOW_LIST, emptySet())
            finalUrl = processQueryStripping(finalUrl, stripList, allowList)
        }
    }
    if (ruleConfig.braveCleanUrls) {
        val rules = decodeCleanUrlsRules(DataStorePreferences.getString(CleanUrlsRuleKeys.RULES, ""))
        if (rules.isNotEmpty()) finalUrl = processCleanUrls(finalUrl, rules)
    }
    if (ruleConfig.braveQueryFilter) {
        val rules = decodeQueryFilterRules(DataStorePreferences.getString(QueryFilterRuleKeys.RULES, ""))
        if (rules.isNotEmpty()) finalUrl = processQueryFilter(finalUrl, rules)
    }
    if (ruleConfig.clearUrls) {
        val catalog = decodeClearUrlsRules(DataStorePreferences.getString(ClearUrlsRuleKeys.RULES, ""))
        if (catalog.providers.isNotEmpty()) finalUrl = processClearUrls(finalUrl, catalog)
    }

    val finalUrlString = finalUrl.toString()
    return applyOriginalSchemeStyle(finalUrlString, hadScheme).ifEmpty { urlString }
}
