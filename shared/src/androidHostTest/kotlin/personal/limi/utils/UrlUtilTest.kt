package personal.limi.utils

import io.ktor.http.Url
import org.junit.Assert.assertEquals
import org.junit.Test

class UrlUtilTest {

    @Test
    fun absoluteLocationKept() {
        val current = Url("http://b23.tv/BV1")
        assertEquals(
            "https://www.bilibili.com/video/BV1",
            resolveRedirect(current, "https://www.bilibili.com/video/BV1").toString()
        )
    }

    @Test
    fun schemeRelativeLocationUsesCurrentScheme() {
        val current = Url("http://b23.tv/BV1")
        assertEquals(
            "http://www.bilibili.com/video/BV1",
            resolveRedirect(current, "//www.bilibili.com/video/BV1").toString()
        )
    }

    @Test
    fun pathRelativeLocationResolvesAgainstCurrentOrigin() {
        val current = Url("http://b23.tv/BV1")
        assertEquals(
            "http://b23.tv/video/BV1",
            resolveRedirect(current, "/video/BV1").toString()
        )
    }

    @Test
    fun schemeLessLocationGetsHttpPrefix() {
        val current = Url("http://b23.tv/BV1")
        assertEquals(
            "http://bilibili.com/video/BV1",
            resolveRedirect(current, "bilibili.com/video/BV1").toString()
        )
    }

    @Test
    fun httpsSelfRedirectChainResolves() {
        val first = resolveRedirect(Url("http://b23.tv/BV1"), "https://b23.tv/BV1")
        val second = resolveRedirect(first, "https://www.bilibili.com/video/BV1")
        assertEquals("https://www.bilibili.com/video/BV1", second.toString())
    }
}
