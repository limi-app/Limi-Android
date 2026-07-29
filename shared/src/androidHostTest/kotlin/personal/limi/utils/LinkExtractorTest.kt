package personal.limi.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LinkExtractorTest {

    @Test
    fun testExtractSimpleUrls() {
        val text = "Visit https://www.google.com for more information"
        val result = extractUrlList(text)
        assertEquals(listOf("https://www.google.com"), result)
    }

    @Test
    fun testExtractMultipleUrls() {
        val text = "Check out https://www.google.com and http://example.com for details"
        val result = extractUrlList(text)
        assertEquals(listOf("https://www.google.com", "http://example.com"), result)
    }

    @Test
    fun testExtractUrlsWithPaths() {
        val text = "Documentation at https://developer.android.com/guide/topics/ui can be helpful"
        val result = extractUrlList(text)
        assertEquals(listOf("https://developer.android.com/guide/topics/ui"), result)
    }

    @Test
    fun testExtractUrlsWithParameters() {
        val text = "Access your account at https://example.com/login?user=test&pass=secret"
        val result = extractUrlList(text)
        assertEquals(listOf("https://example.com/login?user=test&pass=secret"), result)
    }

    @Test
    fun testExtractUrlsWithoutProtocol() {
        val text = "Visit www.example.com for more info"
        val result = extractUrlList(text)
        assertEquals(listOf("www.example.com"), result)
    }

    @Test
    fun testExtractUrlsInBrackets() {
        val text = "Link (https://www.example.com) is here"
        val result = extractUrlList(text)
        assertEquals(listOf("https://www.example.com"), result)
    }

    @Test
    fun testExtractUrlsWithTrailingPunctuation() {
        val text = "Go to https://www.example.com."
        val result = extractUrlList(text)
        assertEquals(listOf("https://www.example.com"), result)
    }

    @Test
    fun testExtractDuplicateUrls() {
        val text = "Visit https://www.example.com and https://www.example.com again"
        val result = extractUrlList(text)
        assertEquals(listOf("https://www.example.com"), result)
    }

    @Test
    fun testExtractNoUrls() {
        val text = "This is just plain text without any URLs"
        val result = extractUrlList(text)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testExtractEmptyText() {
        val text = ""
        val result = extractUrlList(text)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testExtractUrlsWithTruncateNonAsciiDisabled() {
        val text = "Visit https://www.example.com and https://www.例子.com"
        val result = extractUrlList(text, truncateNonAscii = false)
        assertEquals(listOf("https://www.example.com"), result)
    }

    @Test
    fun testExtractUrlsWithNumericTld() {
        val text = "No.2: Visit https://www.example.com for more information"
        val result = extractUrlList(text)
        assertEquals(listOf("https://www.example.com"), result)
    }

    @Test
    fun testExtractUrlsWithNumericTld2() {
        val text = "No.1 No.2 No.3"
        val result = extractUrlList(text)
        assertTrue(result.isEmpty())
    }
}