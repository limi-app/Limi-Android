package personal.limi.logic.rule.cloud

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import personal.limi.logic.rules.cloud.contentHash

class ContentHashTest {
  @Test
  fun deterministicForSameInput() {
    assertEquals(contentHash("{\"a\":1}"), contentHash("{\"a\":1}"))
  }

  @Test
  fun changesWithContent() {
    assertNotEquals(contentHash("{\"a\":1}"), contentHash("{\"a\":2}"))
    assertNotEquals(contentHash("{\"a\":1}"), contentHash("{\"a\":1 }"))
  }

  @Test
  fun producesHexString() {
    val hash = contentHash("hello")
    assert(hash.matches(Regex("[0-9a-f]{64}")))
  }

  @Test
  fun emptyInputHasFixedHash() {
    assertEquals(
      "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
      contentHash(""),
    )
  }
}
