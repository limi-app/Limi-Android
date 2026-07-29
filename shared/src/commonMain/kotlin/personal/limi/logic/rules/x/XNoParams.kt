package personal.limi.logic.rules.x

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import personal.limi.logic.rules.bilibili.UnsupportedURLException

// 对外暴露的目标处理 Host
val xNoParamsTargetHost = arrayOf("www.x.com", "x.com", "www.twitter.com", "twitter.com")

// 最终允许的 Host
private val xNoParamsAllowedHost = arrayOf("www.x.com", "x.com", "www.twitter.com", "twitter.com")

fun processXNoParamsUrl(url: Url): Url {
    if (url.host.lowercase() !in xNoParamsTargetHost) throw UnsupportedURLException("与 xNoParamsTargetHost 链接不匹配")
    val finalUrl = URLBuilder(url).apply { parameters.clear() }.build()
    if (finalUrl.host.lowercase() !in xNoParamsAllowedHost) throw UnsupportedURLException(
        "重定向链接与 x.com 链接不匹配"
    )
    return finalUrl
}