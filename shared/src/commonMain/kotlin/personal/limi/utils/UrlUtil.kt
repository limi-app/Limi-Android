package personal.limi.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.http.Url
import io.ktor.http.authority
import io.ktor.http.isSuccess
import personal.limi.logic.ensureUrlScheme
import personal.limi.logic.hasUrlScheme

private const val MAX_REDIRECTS = 10

/**
 * 获取重定向的 url
 */
suspend fun getRedirectsUrl(url: Url): Url {
    val client = HttpClient(CIO) {
        followRedirects = false
    }
    try {
        var current = url
        repeat(MAX_REDIRECTS) {
            val response = client.get(current)
            when {
                response.status.value in 300..399 -> {
                    val redirectUrl = response.headers["Location"] ?: throw Exception("重定向缺少 Location 头")
                    current = resolveRedirect(current, redirectUrl)
                }

                response.status.isSuccess() -> return current
                else -> throw Exception("未知的错误")
            }
        }
        throw Exception("重定向次数过多")
    } finally {
        client.close()
    }
}

internal fun resolveRedirect(current: Url, location: String): Url {
    val trimmed = location.trim()
    return when {
        trimmed.startsWith("//") -> Url("${current.protocol.name}:$trimmed")
        hasUrlScheme(trimmed) -> Url(trimmed)
        trimmed.startsWith("/") -> Url("${current.protocol.name}://${current.authority}$trimmed")
        else -> Url(ensureUrlScheme(trimmed))
    }
}
