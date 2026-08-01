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
    fun pathRelativeLocationWithoutSlashResolvesAgainstCurrentPath() {
        val current = Url("http://b23.tv/BV1")
        assertEquals(
            "http://b23.tv/video/BV1",
            resolveRedirect(current, "video/BV1").toString()
        )
    }

    @Test
    fun pathRelativeLocationResolvesAgainstCurrentDirectory() {
        val current = Url("http://b23.tv/video/BV1")
        assertEquals(
            "http://b23.tv/video/more/thing",
            resolveRedirect(current, "more/thing").toString()
        )
    }

    @Test
    fun pathRelativeLocationWithQueryResolvesAgainstCurrentPath() {
        val current = Url("http://b23.tv/BV1")
        assertEquals(
            "http://b23.tv/video/BV1?x=1",
            resolveRedirect(current, "video/BV1?x=1").toString()
        )
    }

    @Test
    fun singleSegmentLocationIsTreatedAsHost() {
        val current = Url("http://b23.tv/BV1")
        assertEquals(
            "http://example.com",
            resolveRedirect(current, "example.com").toString()
        )
    }

    @Test
    fun httpsSelfRedirectChainResolves() {
        val first = resolveRedirect(Url("http://b23.tv/BV1"), "https://b23.tv/BV1")
        val second = resolveRedirect(first, "https://www.bilibili.com/video/BV1")
        assertEquals("https://www.bilibili.com/video/BV1", second.toString())
    }
}
