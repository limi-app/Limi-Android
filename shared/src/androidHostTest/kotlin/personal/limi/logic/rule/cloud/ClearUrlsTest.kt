package personal.limi.logic.rule.cloud

import io.ktor.http.Url
import org.junit.Assert.assertEquals
import org.junit.Test
import personal.limi.logic.rules.cloud.clearurlsxyz.ClearUrlsCatalog
import personal.limi.logic.rules.cloud.clearurlsxyz.ClearUrlsProvider
import personal.limi.logic.rules.cloud.clearurlsxyz.parseClearUrls
import personal.limi.logic.rules.cloud.clearurlsxyz.processClearUrls

class ClearUrlsTest {

    private val globalCatalog = ClearUrlsCatalog(
        providers = mapOf(
            "globalRules" to ClearUrlsProvider(
                urlPattern = ".*",
                rules = listOf("(?:%3F)?utm(?:_[a-z_]*)?", "gclid", "yclid")
            )
        )
    )

    @Test
    fun removeGlobalTrackingParams() {
        val url = Url("https://example.com/page?utm_source=news&gclid=1&yclid=2&keep=1")
        assertEquals("https://example.com/page?keep=1", processClearUrls(url, globalCatalog).toString())
    }

    @Test
    fun paramRemovalIsCaseInsensitive() {
        val url = Url("https://example.com/page?UTM_SOURCE=x&Gclid=y&keep=1")
        assertEquals("https://example.com/page?keep=1", processClearUrls(url, globalCatalog).toString())
    }

    @Test
    fun removeSiteSpecificRegexParams() {
        val catalog = ClearUrlsCatalog(
            providers = mapOf(
                "amazon" to ClearUrlsProvider(
                    urlPattern = "^https?:\\/\\/(?:[a-z0-9-]+\\.)*?amazon(?:\\.com)?",
                    rules = listOf("ref", "tag", "p[fd]_rd_[a-z]*")
                )
            )
        )
        val url = Url("https://www.amazon.com/dp/1?ref=a&tag=b&pf_rd_p=1&pf_rd_r=2&keep=1")
        assertEquals("https://www.amazon.com/dp/1?keep=1", processClearUrls(url, catalog).toString())
    }

    @Test
    fun nonMatchingHostUntouched() {
        val catalog = ClearUrlsCatalog(
            providers = mapOf(
                "amazon" to ClearUrlsProvider(
                    urlPattern = "^https?:\\/\\/(?:[a-z0-9-]+\\.)*?amazon(?:\\.com)?",
                    rules = listOf("ref")
                )
            )
        )
        val url = Url("https://example.com/?ref=1")
        assertEquals("https://example.com/?ref=1", processClearUrls(url, catalog).toString())
    }

    @Test
    fun exceptionSkipsProvider() {
        val catalog = ClearUrlsCatalog(
            providers = mapOf(
                "example" to ClearUrlsProvider(
                    urlPattern = ".*",
                    rules = listOf("foo"),
                    exceptions = listOf("^https?://example\\.com/no")
                )
            )
        )
        val excluded = Url("https://example.com/no?foo=1")
        assertEquals("https://example.com/no?foo=1", processClearUrls(excluded, catalog).toString())
        val included = Url("https://example.com/yes?foo=1")
        assertEquals("https://example.com/yes", processClearUrls(included, catalog).toString())
    }

    @Test
    fun applyRawRules() {
        val catalog = ClearUrlsCatalog(
            providers = mapOf(
                "test" to ClearUrlsProvider(
                    urlPattern = ".*",
                    rawRules = listOf("/ref=[^/|?]*")
                )
            )
        )
        val url = Url("https://example.com/path/ref=abc?keep=1")
        assertEquals("https://example.com/path?keep=1", processClearUrls(url, catalog).toString())
    }

    @Test
    fun resolveSimpleRedirection() {
        val catalog = ClearUrlsCatalog(
            providers = mapOf(
                "redirect" to ClearUrlsProvider(
                    urlPattern = ".*",
                    redirections = listOf("^https?://redirect\\.com\\/go\\?url=([^&]+)")
                )
            )
        )
        val url = Url("https://redirect.com/go?url=https%3A%2F%2Fexample.com%2Fpath")
        assertEquals("https://example.com/path", processClearUrls(url, catalog).toString())
    }

    @Test
    fun redirectionWithoutSchemeGetsHttpPrefix() {
        val catalog = ClearUrlsCatalog(
            providers = mapOf(
                "r" to ClearUrlsProvider(
                    urlPattern = ".*",
                    redirections = listOf("^https?://r\\.com\\?to=(.*)")
                )
            )
        )
        val url = Url("https://r.com?to=example.com")
        assertEquals("http://example.com", processClearUrls(url, catalog).toString())
    }

    @Test
    fun doubleEncodedRedirectionDecodedRecursively() {
        val catalog = ClearUrlsCatalog(
            providers = mapOf(
                "r" to ClearUrlsProvider(
                    urlPattern = ".*",
                    redirections = listOf("^https?://r\\.com\\?to=(.*)")
                )
            )
        )
        val url = Url("https://r.com?to=https%253A%252F%252Fexample.com")
        assertEquals("https://example.com", processClearUrls(url, catalog).toString())
    }

    @Test
    fun redirectionPreservesSchemeCaseInsensitively() {
        val catalog = ClearUrlsCatalog(
            providers = mapOf(
                "r" to ClearUrlsProvider(
                    urlPattern = ".*",
                    redirections = listOf("^https?://r\\.com\\?to=(.*)")
                )
            )
        )
        val url = Url("https://r.com?to=HTTPS%3A%2F%2FExample.com")
        assertEquals("https://Example.com", processClearUrls(url, catalog).toString())
    }

    @Test
    fun redirectionPreservesMailtoScheme() {
        val catalog = ClearUrlsCatalog(
            providers = mapOf(
                "r" to ClearUrlsProvider(
                    urlPattern = ".*",
                    redirections = listOf("^https?://r\\.com\\?to=(.*)")
                )
            )
        )
        val url = Url("https://r.com?to=mailto%3Auser%40example.com")
        assertEquals("mailto:user@example.com", processClearUrls(url, catalog).toString())
    }

    @Test
    fun schemeRelativeRedirectionDefaultsToHttps() {
        val catalog = ClearUrlsCatalog(
            providers = mapOf(
                "r" to ClearUrlsProvider(
                    urlPattern = ".*",
                    redirections = listOf("^https?://r\\.com\\?to=(.*)")
                )
            )
        )
        val url = Url("https://r.com?to=%2F%2Fexample.com%2Fpath")
        assertEquals("https://example.com/path", processClearUrls(url, catalog).toString())
    }

    @Test
    fun multiLayerEncodedHostGetsHttpPrefix() {
        val catalog = ClearUrlsCatalog(
            providers = mapOf(
                "r" to ClearUrlsProvider(
                    urlPattern = ".*",
                    redirections = listOf("^https?://r\\.com\\?to=(.*)")
                )
            )
        )
        val url = Url("https://r.com?to=e%2578ample.com")
        assertEquals("http://example.com", processClearUrls(url, catalog).toString())
    }

    @Test
    fun recursiveCleaningAfterRedirection() {
        val catalog = ClearUrlsCatalog(
            providers = mapOf(
                "go" to ClearUrlsProvider(
                    urlPattern = "^https?://go\\.com",
                    redirections = listOf("^https?://go\\.com\\?url=([^&]+)")
                ),
                "global" to ClearUrlsProvider(
                    urlPattern = ".*",
                    rules = listOf("utm_(?:source|medium|campaign)")
                )
            )
        )
        val url = Url("https://go.com?url=https%3A%2F%2Fexample.com%3Futm_source%3Dx%26keep%3D1")
        assertEquals("https://example.com?keep=1", processClearUrls(url, catalog).toString())
    }

    @Test
    fun removeFragmentParams() {
        val catalog = ClearUrlsCatalog(
            providers = mapOf("g" to ClearUrlsProvider(urlPattern = ".*", rules = listOf("ref")))
        )
        val url = Url("https://example.com/page#ref=1&keep=2")
        assertEquals("https://example.com/page#keep=2", processClearUrls(url, catalog).toString())
    }

    @Test
    fun completeProviderSkipped() {
        val catalog = ClearUrlsCatalog(
            providers = mapOf(
                "blocked" to ClearUrlsProvider(
                    urlPattern = ".*",
                    completeProvider = true,
                    rules = listOf("ref")
                )
            )
        )
        val url = Url("https://example.com/?ref=1")
        assertEquals("https://example.com/?ref=1", processClearUrls(url, catalog).toString())
    }

    @Test
    fun invalidRegexIsTolerated() {
        val catalog = ClearUrlsCatalog(
            providers = mapOf(
                "bad" to ClearUrlsProvider(
                    urlPattern = ".*",
                    rules = listOf("[invalid", "ref")
                )
            )
        )
        val url = Url("https://example.com/?ref=1&keep=2")
        assertEquals("https://example.com/?keep=2", processClearUrls(url, catalog).toString())
    }

    @Test
    fun parseRealCatalogShapeAndProcess() {
        val json = """
            {
              "providers": {
                "globalRules": {
                  "urlPattern": ".*",
                  "rules": ["(?:%3F)?utm(?:_[a-z_]*)?", "(?:%3F)?yclid"],
                  "referralMarketing": ["(?:%3F)?ref_?", "(?:%3F)?referrer"],
                  "exceptions": []
                },
                "amazon": {
                  "urlPattern": "^https?:\\/\\/(?:[a-z0-9-]+\\.)*?amazon(?:\\.com)?",
                  "completeProvider": false,
                  "rules": ["ref", "tag"],
                  "rawRules": ["/ref=[^/|?]*"],
                  "referralMarketing": [],
                  "exceptions": [],
                  "redirections": []
                },
                "blocked": {
                  "urlPattern": "^https?:\\/\\/ads\\.example\\.com",
                  "completeProvider": true
                }
              }
            }
        """.trimIndent()
        val catalog = parseClearUrls(json)
        assertEquals(3, catalog.providers.size)
        val url = Url("https://www.amazon.com/dp/1?ref=a&tag=b&utm_source=x&keep=1")
        assertEquals("https://www.amazon.com/dp/1?keep=1", processClearUrls(url, catalog).toString())
    }

    @Test
    fun referralMarketingParamsRemovedByDefault() {
        val catalog = parseClearUrls(
            """{"providers":{"globalRules":{"urlPattern":".*","referralMarketing":["(?:%3F)?ref_?"]}}}"""
        )
        val url = Url("https://example.com/?ref=abc&keep=1")
        assertEquals("https://example.com/?keep=1", processClearUrls(url, catalog).toString())
    }
}
