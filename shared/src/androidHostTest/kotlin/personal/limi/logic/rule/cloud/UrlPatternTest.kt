package personal.limi.logic.rule.cloud

import io.ktor.http.Url
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import personal.limi.logic.rules.cloud.matchesPattern

class UrlPatternTest {
  @Test
  fun matchExactHost() {
    val url = Url("https://example.com/path?a=1")
    assertTrue(matchesPattern(url, "*://example.com/*"))
    assertTrue(matchesPattern(url, "https://example.com/*"))
    assertFalse(matchesPattern(url, "http://example.com/*"))
    assertFalse(matchesPattern(url, "*://example.org/*"))
  }

  @Test
  fun matchSubdomainWildcard() {
    val url = Url("https://www.facebook.com/page")
    assertTrue(matchesPattern(url, "*://*.facebook.com/*"))
    assertFalse(matchesPattern(url, "*://facebook.com/*"))
    assertFalse(matchesPattern(url, "*://*.facebooks.com/*"))
  }

  @Test
  fun matchAnyHost() {
    val url = Url("https://foo.example.com/x")
    assertTrue(matchesPattern(url, "*://*/*"))
  }

  @Test
  fun matchPathWithQuery() {
    val url = Url("https://www.google.com/search?q=hello&source=web")
    assertTrue(matchesPattern(url, "*://www.google.com/search?*"))
    assertFalse(matchesPattern(url, "*://www.google.com/maps?*"))
  }

  @Test
  fun matchPathWildcardSegment() {
    val url = Url("https://ctr.narvar.com/v2/tracking/123")
    assertTrue(matchesPattern(url, "*://ctr.narvar.com/*/tracking/*"))
  }

  @Test
  fun excludePatterns() {
    val url = Url("https://www.x.com/i/redirect?url=x")
    assertTrue(matchesPattern(url, "*://*.x.com/*"))
    assertTrue(matchesPattern(url, "*://*.x.com/i/redirect?*"))
  }
}
