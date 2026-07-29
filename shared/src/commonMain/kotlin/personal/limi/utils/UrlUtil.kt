package personal.limi.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.http.Url
import io.ktor.http.isSuccess

/**
 * 获取重定向的 url
 */
suspend fun getRedirectsUrl(url: Url): Url {
    val client = HttpClient(CIO) {
        followRedirects = false
    }
    try {
        val response = client.get(url)
        if (response.status.value in 300..399) {
            val redirectUrl = response.headers["Location"]
            if (redirectUrl != null) return Url(redirectUrl)
        } else if (response.status.isSuccess()) return url
        throw Exception("未知的错误")
    } finally {
        client.close()
    }
}
