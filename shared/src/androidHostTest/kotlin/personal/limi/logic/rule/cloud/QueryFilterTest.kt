package personal.limi.logic.rule.cloud

import io.ktor.http.Url
import org.junit.Assert.assertEquals
import org.junit.Test
import personal.limi.logic.rules.cloud.brave.BraveQueryRule
import personal.limi.logic.rules.cloud.brave.processQueryFilter

class QueryFilterTest {
  @Test
  fun stripGlobalParams() {
    val rules =
      listOf(
        BraveQueryRule(
          include = listOf("*://*/*"),
          params = listOf("fbclid", "gclid"),
        )
      )
    val url = Url("https://example.com/?fbclid=1&gclid=2&keep=1")
    assertEquals("https://example.com/?keep=1", processQueryFilter(url, rules).toString())
  }

  @Test
  fun siteSpecificOverride() {
    val rules =
      listOf(
        BraveQueryRule(
          include = listOf("*://*/*"),
          params = listOf("fbclid"),
        ),
        BraveQueryRule(
          include = listOf("*://*.youtube.com/*", "*://*.youtu.be/*"),
          params = listOf("si"),
        ),
      )
    val youtube = Url("https://www.youtube.com/watch?v=abc&si=xyz&fbclid=1")
    assertEquals(
      "https://www.youtube.com/watch?v=abc",
      processQueryFilter(youtube, rules).toString(),
    )
  }
}
