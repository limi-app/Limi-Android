package personal.limi.logic.rules.common

import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.Url

fun processCommonParams(url: Url): Url {
    val targetParams = listOf(
        "gclid", // Google
        "dclid", // Google
        "wbraid", // Google
        "gbraid", // Google
        "gad_source", // Google
        "gad_campaignid", // Google
        "gclsrc", // Google
        "fbclid", // Meta
        "ttclid", // TikTok
        "msclkid", // Microsoft
        "li_fat_id", // LinkedIn
        "twclid", // X (formerly Twitter)
        "epik", // Pinterest
        "ScCid", // Snapchat
        "yclid", // Yandex  / Yahoo! Japan
        "ymclid", // Yandex
        "sznclid", // Seznam / Sklik
        "mc_cid", // Mailchimp
        "mc_eid", // Mailchimp
        "wickedid", // Wicked
        "tblclid", // Taboola
        "dicbo", // Outbrain
        "extclid", // Affiliate
        "aa_campaignid", // Amazon
        "aa_adgroupid", // Amazon
        "maas", // Amazon
        "traffic_type", // Affiliate
        "traffic_source", // Affiliate
        "unique_aff_sub1", // Affiliate
        "unique_aff_sub2", // Affiliate
        "unique_aff_sub3", // Affiliate
        "unique_aff_sub4", // Affiliate
        "unique_aff_sub5", // Affiliate
        "ha_source", // HUAWEI
        "ha_sourceId", // HUAWEI
        "bd_vid", // Baidu
    )
    val filteredParameters = Parameters.build {
        url.parameters.forEach { key, values -> if (key !in targetParams) appendAll(key, values) }
    }

    return URLBuilder(url).apply {
        parameters.clear()
        parameters.appendAll(filteredParameters)
    }.build()
}