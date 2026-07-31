package personal.limi.logic.rule.cloud

import io.ktor.http.Url
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import personal.limi.logic.rules.cloud.brave.DebounceRule
import personal.limi.logic.rules.cloud.brave.processDebounce

class DebounceTest {
    @Test
    fun redirectParam() {
        val rules = listOf(
            DebounceRule(
                include = listOf("*://go.skimresources.com/*"),
                action = "redirect",
                param = "url"
            )
        )
        val url = Url("https://go.skimresources.com/?url=https%3A%2F%2Fexample.com%2Fpage%3Futm_source%3Dx")
        assertEquals(
            "https://example.com/page?utm_source=x",
            processDebounce(url, rules)?.toString()
        )
    }

    @Test
    fun noRedirectWhenParamMissing() {
        val rules = listOf(
            DebounceRule(
                include = listOf("*://go.skimresources.com/*"),
                action = "redirect",
                param = "url"
            )
        )
        val url = Url("https://go.skimresources.com/?id=1")
        assertNull(processDebounce(url, rules))
    }

    @Test
    fun skipNonMatchingHost() {
        val rules = listOf(
            DebounceRule(
                include = listOf("*://go.skimresources.com/*"),
                action = "redirect",
                param = "url"
            )
        )
        val url = Url("https://example.com/?url=https%3A%2F%2Ftarget.com")
        assertNull(processDebounce(url, rules))
    }

    @Test
    fun regexPathAmp() {
        val rules = listOf(
            DebounceRule(
                include = listOf("*://*.cdn.ampproject.org/c/s/*"),
                prepend_scheme = "https",
                action = "regex-path",
                param = "^/c/s/(.*)$"
            )
        )
        val url = Url("https://example-com.cdn.ampproject.org/c/s/example.com/article?id=1")
        assertEquals("https://example.com/article?id=1", processDebounce(url, rules)?.toString())
    }

    @Test
    fun regexPathTemplate() {
        val rules = listOf(
            DebounceRule(
                include = listOf("*://y2u.be/*"),
                action = "regex-path-template",
                param = "^/([^/]+)$",
                redirect_url_template = "https://www.youtube.com/watch?v=$1"
            )
        )
        val url = Url("https://y2u.be/abc123")
        assertEquals("https://www.youtube.com/watch?v=abc123", processDebounce(url, rules)?.toString())
    }

    @Test
    fun base64Redirect() {
        val rules = listOf(
            DebounceRule(
                include = listOf("*://*.ouo.today/*"),
                action = "base64,redirect",
                param = "cr"
            )
        )
        val url = Url("https://ouo.today/x?cr=aHR0cHM6Ly9leGFtcGxlLmNvbS9wYWdl")
        assertEquals("https://example.com/page", processDebounce(url, rules)?.toString())
    }

    @Test
    fun base64RedirectUrlSafeUnpadded() {
        val rules = listOf(
            DebounceRule(
                include = listOf("*://*.ouo.today/*"),
                action = "base64,redirect",
                param = "cr"
            )
        )
        val url = Url("https://ouo.today/x?cr=aHR0cHM6Ly9leGFtcGxlLmNvbS8_a2V5PXZhbHVlJmZvbz1iYXI")
        assertEquals("https://example.com/?key=value&foo=bar", processDebounce(url, rules)?.toString())
    }
}
