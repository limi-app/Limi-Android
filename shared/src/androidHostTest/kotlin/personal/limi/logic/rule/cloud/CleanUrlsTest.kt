package personal.limi.logic.rule.cloud

import io.ktor.http.Url
import org.junit.Assert.assertEquals
import org.junit.Test
import personal.limi.logic.rules.cloud.brave.BraveQueryRule
import personal.limi.logic.rules.cloud.brave.processCleanUrls

class CleanUrlsTest {
  @Test
  fun stripSiteSpecificParams() {
    val rules =
      listOf(
        BraveQueryRule(
          include = listOf("*://*.amazon.com/*"),
          params = listOf("ref", "tag"),
        )
      )
    val url = Url("https://www.amazon.com/dp/123?ref=abc&tag=xyz&keep=1")
    assertEquals("https://www.amazon.com/dp/123?keep=1", processCleanUrls(url, rules).toString())
  }

  @Test
  fun skipNonMatchingHost() {
    val rules =
      listOf(
        BraveQueryRule(
          include = listOf("*://*.amazon.com/*"),
          params = listOf("ref"),
        )
      )
    val url = Url("https://example.com/?ref=abc")
    assertEquals("https://example.com/?ref=abc", processCleanUrls(url, rules).toString())
  }

  @Test
  fun respectExclude() {
    val rules =
      listOf(
        BraveQueryRule(
          include = listOf("*://*/*"),
          exclude = listOf("https://app.hive.co/*"),
          params = listOf("h_sid"),
        )
      )
    val excluded = Url("https://app.hive.co/page?h_sid=1")
    assertEquals("https://app.hive.co/page?h_sid=1", processCleanUrls(excluded, rules).toString())
    val included = Url("https://example.com/page?h_sid=1")
    assertEquals("https://example.com/page", processCleanUrls(included, rules).toString())
  }

  @Test
  fun cumulativeAcrossRules() {
    val rules =
      listOf(
        BraveQueryRule(
          include = listOf("*://*.amazon.com/*"),
          params = listOf("ref"),
        ),
        BraveQueryRule(
          include = listOf("*://*/*"),
          params = listOf("gclid"),
        ),
      )
    val url = Url("https://www.amazon.com/dp/1?ref=abc&gclid=xyz&keep=1")
    assertEquals("https://www.amazon.com/dp/1?keep=1", processCleanUrls(url, rules).toString())
  }
}
