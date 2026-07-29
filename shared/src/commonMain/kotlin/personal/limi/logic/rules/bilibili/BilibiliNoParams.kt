package personal.limi.logic.rules.bilibili

import io.ktor.http.URLBuilder
import io.ktor.http.Url

// 对外暴露的目标处理 Host
val bilibiliNoParamsTargetHost = bilibiliRedirectAllowedHost

// 最终允许的 Host
private val bilibiliNoParamsAllowedHost = bilibiliRedirectAllowedHost

// 需要保留的查询参数
private val retainedParams = setOf(
    "business_id", "business_type", "itemsId", "lottery_id", "p", "start_progress", "t"
)

fun processBilibiliNoParamsUrl(url: Url): Url {
    if (url.host.lowercase() !in bilibiliNoParamsTargetHost) throw UnsupportedURLException("与 bilibiliNoParamsTargetHost 链接不匹配")
    val finalUrl = URLBuilder(url).apply {
        val retained = parameters.entries().filter { (key, _) -> key in retainedParams }
        parameters.clear()
        retained.forEach { (key, values) -> values.forEach { parameters.append(key, it) } }
        // 移除值为 "1" 的 p 参数
        if (parameters["p"] == "1") parameters.remove("p")
    }.build()
    if (finalUrl.host.lowercase() !in bilibiliNoParamsAllowedHost) throw UnsupportedURLException(
        "重定向链接与 bilibili.com 链接不匹配"
    )
    return finalUrl
}