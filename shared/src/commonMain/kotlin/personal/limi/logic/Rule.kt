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

    var finalUrl = Url(urlString)

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
    return finalUrlString.ifEmpty { urlString }
}
