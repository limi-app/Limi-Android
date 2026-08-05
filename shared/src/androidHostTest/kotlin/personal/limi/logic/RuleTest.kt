package personal.limi.logic

import io.ktor.http.Url
import org.junit.Assert.assertEquals
import org.junit.Test
import personal.limi.logic.rules.common.processUTMParams

class RuleTest {

  @Test
  fun schemeLessHostGetsHttpPrefix() {
    assertEquals("http://a.com", ensureUrlScheme("a.com"))
  }

  @Test
  fun schemeLessHostWithPathGetsHttpPrefix() {
    assertEquals("http://a.com/path?q=1", ensureUrlScheme("a.com/path?q=1"))
  }

  @Test
  fun schemeLessHostWithPortGetsHttpPrefix() {
    assertEquals("http://localhost:3000", ensureUrlScheme("localhost:3000"))
  }

  @Test
  fun schemeLessHostPortWithDotGetsHttpPrefix() {
    assertEquals("http://a.com:8080", ensureUrlScheme("a.com:8080"))
  }

  @Test
  fun schemeLessSingleLabelHostWithPortGetsHttpPrefix() {
    assertEquals("http://intranet:8080", ensureUrlScheme("intranet:8080"))
  }

  @Test
  fun httpUrlKept() {
    assertEquals("http://a.com/x", ensureUrlScheme("http://a.com/x"))
  }

  @Test
  fun httpsUrlKept() {
    assertEquals("https://a.com/x", ensureUrlScheme("https://a.com/x"))
  }

  @Test
  fun mailtoKept() {
    assertEquals("mailto:user@example.com", ensureUrlScheme("mailto:user@example.com"))
  }

  @Test
  fun customSchemeWithSlashSlashKept() {
    assertEquals("taobao://item.htm?id=1", ensureUrlScheme("taobao://item.htm?id=1"))
  }

  @Test
  fun customSchemeDeepLinkKept() {
    assertEquals("myapp:deep/path", ensureUrlScheme("myapp:deep/path"))
  }

  @Test
  fun customSchemeWithActionKept() {
    assertEquals("customapp:action?param=1", ensureUrlScheme("customapp:action?param=1"))
  }

  @Test
  fun wwwHostGetsHttpPrefix() {
    assertEquals("http://www.example.com", ensureUrlScheme("www.example.com"))
  }

  @Test
  fun emailIsNotTreatedAsUrl() {
    val input = "e@a.com"
    assertEquals(false, hasUrlScheme(input))
    val parsed = Url(ensureUrlScheme(input)).toString()
    assertEquals("e@a.com", applyOriginalSchemeStyle(parsed, hasUrlScheme(input)))
  }

  @Test
  fun schemeLessLinkKeepsOriginalStyle() {
    val input = "a.com"
    val parsed = Url(ensureUrlScheme(input)).toString()
    assertEquals("a.com", applyOriginalSchemeStyle(parsed, hasUrlScheme(input)))
  }

  @Test
  fun schemeLessTrackingLinkStillProcessedAndStyleKept() {
    val input = "a.com?utm_source=x&keep=1"
    val processed = processUTMParams(Url(ensureUrlScheme(input))).toString()
    assertEquals("a.com?keep=1", applyOriginalSchemeStyle(processed, hasUrlScheme(input)))
  }

  @Test
  fun schemeUrlKeepsScheme() {
    val input = "https://a.com?utm_source=x&keep=1"
    val processed = processUTMParams(Url(input)).toString()
    assertEquals("https://a.com?keep=1", applyOriginalSchemeStyle(processed, hasUrlScheme(input)))
  }

  @Test
  fun schemeLessInputStripsHttpsFromRedirectResult() {
    assertEquals("example.com/path", applyOriginalSchemeStyle("https://example.com/path", false))
  }

  @Test
  fun schemeLessInputStripsOtherScheme() {
    assertEquals("example.com/path", applyOriginalSchemeStyle("ftp://example.com/path", false))
  }

  @Test
  fun schemeLessInputKeepsSchemeLessResultUnchanged() {
    assertEquals("example.com/path", applyOriginalSchemeStyle("example.com/path", false))
  }
}
