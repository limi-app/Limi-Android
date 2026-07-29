package personal.limi.logic.rules.bilibili

import io.ktor.http.URLBuilder
import io.ktor.http.Url

// 对外暴露的目标处理 Host
val bilibiliNoParamsTargetHost = bilibiliRedirectAllowedHost

// 最终允许的 Host
private val bilibiliNoParamsAllowedHost = bilibiliRedirectAllowedHost

fun processBilibiliNoParamsUrl(url: Url): Url {
    if (url.host.lowercase() !in bilibiliNoParamsTargetHost) throw UnsupportedURLException("与 bilibiliNoParamsTargetHost 链接不匹配")
    val finalUrl = URLBuilder(url).apply { parameters.clear() }.build()
    if (finalUrl.host.lowercase() !in bilibiliNoParamsAllowedHost) throw UnsupportedURLException(
        "重定向链接与 bilibili.com 链接不匹配"
    )
    return finalUrl
}