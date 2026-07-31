package personal.limi.logic.rule.cloud

import io.ktor.http.Url
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import personal.limi.logic.rules.cloud.parseQueryStrippingRecords
import personal.limi.logic.rules.cloud.processQueryStripping

class QueryStrippingTest {
    @Test
    fun parseRecordsMergesStripListAndAllowList() {
        val body = """{
            "data": [
              {"schema":1,"allowList":["googleadservices.com"],"stripList":[],"id":"a","last_modified":100},
              {"schema":2,"allowList":[],"stripList":["gclid","dclid"],"id":"b","last_modified":200},
              {"schema":3,"allowList":[],"stripList":["fbclid","mkt_tok"],"id":"c","last_modified":300}
            ]
        }"""
        val data = parseQueryStrippingRecords(body)

        assertEquals(setOf("gclid", "dclid", "fbclid", "mkt_tok"), data.stripList)
        assertEquals(setOf("googleadservices.com"), data.allowList)
        assertEquals(300L, data.lastModified)
    }

    @Test
    fun stripParamsFromStripList() {
        val url = Url("https://example.com/?gclid=123&fbclid=abc&foo=bar")
        val result = processQueryStripping(url, setOf("gclid", "fbclid"), emptySet())

        assertEquals("https://example.com/?foo=bar", result.toString())
    }

    @Test
    fun keepUrlUnchangedWhenHostInAllowList() {
        val url = Url("https://googleadservices.com/?gclid=123")
        val result = processQueryStripping(url, setOf("gclid"), setOf("googleadservices.com"))

        assertEquals("https://googleadservices.com/?gclid=123", result.toString())
        assertTrue(result.parameters.contains("gclid"))
    }

    @Test
    fun keepUrlUnchangedWhenNoMatchingParams() {
        val url = Url("https://example.com/?foo=bar")
        val result = processQueryStripping(url, setOf("gclid"), emptySet())

        assertEquals("https://example.com/?foo=bar", result.toString())
    }
}
