package personal.limi.logic.rule.common

import io.ktor.http.Url
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import personal.limi.logic.rules.common.processUTMParams

class UTMParamsTest {
    @Test
    fun removeAllUTMParameters() {
        val originalUrl = Url("https://example.com/?utm_source=google&utm_medium=cpc&utm_campaign=summer_sale&utm_term=running+shoes&utm_content=logolink")
        val result = processUTMParams(originalUrl)

        assertEquals("https://example.com/", result.toString())
        assertTrue(result.parameters.names().isEmpty())
    }

    @Test
    fun keepNonUTMParameters() {
        val originalUrl = Url("https://example.com/?param1=value1&utm_source=google&param2=value2")
        val result = processUTMParams(originalUrl)

        assertFalse(result.parameters.names().isEmpty())
        assertEquals("value1", result.parameters["param1"])
        assertEquals("value2", result.parameters["param2"])
        assertNull(result.parameters["utm_source"])
    }

    @Test
    fun keepUrlUnchangedWhenNoUTMParameters() {
        val originalUrl = Url("https://example.com/?param1=value1&param2=value2")
        val result = processUTMParams(originalUrl)

        assertEquals(originalUrl.toString(), result.toString())
        assertEquals("value1", result.parameters["param1"])
        assertEquals("value2", result.parameters["param2"])
    }

    @Test
    fun removePartialUTMParameters() {
        val originalUrl = Url("https://example.com/?utm_source=google&param1=value1")
        val result = processUTMParams(originalUrl)

        assertEquals("https://example.com/?param1=value1", result.toString())
        assertNull(result.parameters["utm_source"])
        assertEquals("value1", result.parameters["param1"])
    }

    @Test
    fun handleEmptyParameters() {
        val originalUrl = Url("https://example.com/")
        val result = processUTMParams(originalUrl)

        assertEquals(originalUrl.toString(), result.toString())
        assertTrue(result.parameters.names().isEmpty())
    }

    @Test
    fun handleMultipleValuesParameter() {
        val originalUrl = Url("https://example.com/?param1=value1&param1=value2&utm_source=google")
        val result = processUTMParams(originalUrl)

        assertFalse(result.parameters.contains("utm_source"))
        assertTrue(result.parameters.contains("param1"))
        assertEquals(2, result.parameters.getAll("param1")?.size)
    }
}
