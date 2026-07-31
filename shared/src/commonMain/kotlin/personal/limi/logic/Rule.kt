package personal.limi.logic

import io.ktor.http.Url
import personal.limi.data.rules.RuleConfig
import personal.limi.logic.rules.bilibili.bilibiliNoParamsTargetHost
import personal.limi.logic.rules.bilibili.bilibiliRedirectTargetHost
import personal.limi.logic.rules.bilibili.processBilibiliNoParamsUrl
import personal.limi.logic.rules.bilibili.processBilibiliRedirectUrl
import personal.limi.logic.rules.cloud.CloudRuleKeys
import personal.limi.logic.rules.cloud.processQueryStripping
import personal.limi.logic.rules.common.processUTMParams
import personal.limi.logic.rules.common.processUTMParamsEnhanced
import personal.limi.logic.rules.x.processXNoParamsUrl
import personal.limi.logic.rules.x.xNoParamsTargetHost
import personal.limi.utils.datastore.DataStorePreferences

object RuleIds {
    const val BILIBILI = "bilibili_rules"
    const val UTM_PARAMS = "utm_params_rules"
    const val UTM_PARAMS_ENHANCED = "utm_params_enhanced_rules"
    const val X = "x_rules"
    const val FIREFOX_QUERY_STRIPPING = "firefox_query_stripping_rules"
}

suspend fun processUrl(urlString: String): String {

    val ruleConfig = RuleConfig(
        UTMParams = DataStorePreferences.getBoolean(RuleIds.UTM_PARAMS, true),
        UTMParamsEnhanced = DataStorePreferences.getBoolean(RuleIds.UTM_PARAMS_ENHANCED, false),
        bilibili = DataStorePreferences.getBoolean(RuleIds.BILIBILI, true),
        x = DataStorePreferences.getBoolean(RuleIds.X, true),
        firefoxQueryStripping = DataStorePreferences.getBoolean(RuleIds.FIREFOX_QUERY_STRIPPING, false)
    )

    var finalUrl = Url(urlString)

    // 第一层：特定 Host 匹配处理
    finalUrl = when {
        ruleConfig.bilibili && finalUrl.host.lowercase() in bilibiliRedirectTargetHost -> processBilibiliRedirectUrl(
            finalUrl
        )

        ruleConfig.bilibili && finalUrl.host.lowercase() in bilibiliNoParamsTargetHost -> processBilibiliNoParamsUrl(
            finalUrl
        )

        ruleConfig.x && finalUrl.host.lowercase() in xNoParamsTargetHost -> processXNoParamsUrl(
            finalUrl
        )

        else -> finalUrl
    }

    // 第二层：通用参数去除处理
    if (ruleConfig.UTMParams) finalUrl = processUTMParams(finalUrl)
    if (ruleConfig.UTMParamsEnhanced) finalUrl = processUTMParamsEnhanced(finalUrl)
    if (ruleConfig.firefoxQueryStripping) {
        val stripList = DataStorePreferences.getStringSet(CloudRuleKeys.QUERY_STRIPPING_STRIP_LIST, emptySet())
        if (stripList.isNotEmpty()) {
            val allowList =
                DataStorePreferences.getStringSet(CloudRuleKeys.QUERY_STRIPPING_ALLOW_LIST, emptySet())
            finalUrl = processQueryStripping(finalUrl, stripList, allowList)
        }
    }

    val finalUrlString = finalUrl.toString()
    return finalUrlString.ifEmpty { urlString }
}
