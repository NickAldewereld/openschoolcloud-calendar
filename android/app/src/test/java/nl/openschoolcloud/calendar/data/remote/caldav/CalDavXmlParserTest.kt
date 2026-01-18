package nl.openschoolcloud.calendar.data.remote.caldav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for CalDavXmlParser
 *
 * Uses Robolectric to provide android.util.Xml on JVM
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class CalDavXmlParserTest {

    private lateinit var parser: CalDavXmlParser

    @Before
    fun setup() {
        parser = CalDavXmlParser()
    }

    // ==================== parseCurrentUserPrincipal tests ====================

    @Test
    fun `parseCurrentUserPrincipal extracts href from valid response`() {
        val response = """
            <?xml version="1.0"?>
            <d:multistatus xmlns:d="DAV:">
              <d:response>
                <d:href>/remote.php/dav/</d:href>
                <d:propstat>
                  <d:prop>
                    <d:current-user-principal>
                      <d:href>/remote.php/dav/principals/users/testuser/</d:href>
                    </d:current-user-principal>
                  </d:prop>
                  <d:status>HTTP/1.1 200 OK</d:status>
                </d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val result = parser.parseCurrentUserPrincipal(response)

        assertNotNull(result)
        assertEquals("/remote.php/dav/principals/users/testuser/", result)
    }

    @Test
    fun `parseCurrentUserPrincipal returns null for missing element`() {
        val response = """
            <?xml version="1.0"?>
            <d:multistatus xmlns:d="DAV:">
              <d:response>
                <d:href>/remote.php/dav/</d:href>
                <d:propstat>
                  <d:prop>
                  </d:prop>
                </d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val result = parser.parseCurrentUserPrincipal(response)

        assertNull(result)
    }

    @Test
    fun `parseCurrentUserPrincipal handles empty response`() {
        val result = parser.parseCurrentUserPrincipal("")
        assertNull(result)
    }

    // ==================== parseCalendarHomeSet tests ====================

    @Test
    fun `parseCalendarHomeSet extracts href from valid response`() {
        val response = """
            <?xml version="1.0"?>
            <d:multistatus xmlns:d="DAV:" xmlns:cal="urn:ietf:params:xml:ns:caldav">
              <d:response>
                <d:propstat>
                  <d:prop>
                    <cal:calendar-home-set>
                      <d:href>/remote.php/dav/calendars/testuser/</d:href>
                    </cal:calendar-home-set>
                  </d:prop>
                </d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val result = parser.parseCalendarHomeSet(response)

        assertNotNull(result)
        assertEquals("/remote.php/dav/calendars/testuser/", result)
    }

    @Test
    fun `parseCalendarHomeSet returns null for missing element`() {
        val response = """
            <?xml version="1.0"?>
            <d:multistatus xmlns:d="DAV:">
              <d:response>
                <d:propstat>
                  <d:prop>
                  </d:prop>
                </d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val result = parser.parseCalendarHomeSet(response)

        assertNull(result)
    }

    // ==================== parseCalendars tests ====================

    @Test
    fun `parseCalendars extracts single calendar with all properties`() {
        val response = """
            <?xml version="1.0"?>
            <d:multistatus xmlns:d="DAV:" xmlns:cal="urn:ietf:params:xml:ns:caldav" xmlns:cs="http://calendarserver.org/ns/" xmlns:x1="http://apple.com/ns/ical/">
              <d:response>
                <d:href>/remote.php/dav/calendars/testuser/personal/</d:href>
                <d:propstat>
                  <d:prop>
                    <d:resourcetype><cal:calendar/></d:resourcetype>
                    <d:displayname>My Calendar</d:displayname>
                    <x1:calendar-color>#0082c9</x1:calendar-color>
                    <cs:getctag>12345</cs:getctag>
                    <d:sync-token>token123</d:sync-token>
                    <d:current-user-privilege-set>
                      <d:privilege><d:read/></d:privilege>
                      <d:privilege><d:write/></d:privilege>
                    </d:current-user-privilege-set>
                  </d:prop>
                  <d:status>HTTP/1.1 200 OK</d:status>
                </d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val result = parser.parseCalendars(response, "https://cloud.school.nl")

        assertEquals(1, result.size)
        val calendar = result[0]
        assertEquals("My Calendar", calendar.displayName)
        assertEquals("#0082c9", calendar.color)
        assertEquals("12345", calendar.ctag)
        assertEquals("token123", calendar.syncToken)
        assertFalse(calendar.readOnly)
    }

    @Test
    fun `parseCalendars filters non-calendar resources`() {
        val response = """
            <?xml version="1.0"?>
            <d:multistatus xmlns:d="DAV:" xmlns:cal="urn:ietf:params:xml:ns:caldav">
              <d:response>
                <d:href>/remote.php/dav/calendars/testuser/</d:href>
                <d:propstat>
                  <d:prop>
                    <d:resourcetype><d:collection/></d:resourcetype>
                    <d:displayname>Calendars Root</d:displayname>
                  </d:prop>
                </d:propstat>
              </d:response>
              <d:response>
                <d:href>/remote.php/dav/calendars/testuser/personal/</d:href>
                <d:propstat>
                  <d:prop>
                    <d:resourcetype><cal:calendar/></d:resourcetype>
                    <d:displayname>Personal</d:displayname>
                  </d:prop>
                </d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val result = parser.parseCalendars(response, "https://cloud.school.nl")

        assertEquals(1, result.size)
        assertEquals("Personal", result[0].displayName)
    }

    @Test
    fun `parseCalendars detects read-only calendar`() {
        val response = """
            <?xml version="1.0"?>
            <d:multistatus xmlns:d="DAV:" xmlns:cal="urn:ietf:params:xml:ns:caldav">
              <d:response>
                <d:href>/remote.php/dav/calendars/testuser/shared/</d:href>
                <d:propstat>
                  <d:prop>
                    <d:resourcetype><cal:calendar/></d:resourcetype>
                    <d:displayname>Shared Calendar</d:displayname>
                    <d:current-user-privilege-set>
                      <d:privilege><d:read/></d:privilege>
                    </d:current-user-privilege-set>
                  </d:prop>
                </d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val result = parser.parseCalendars(response, "https://cloud.school.nl")

        assertEquals(1, result.size)
        assertTrue(result[0].readOnly)
    }

    @Test
    fun `parseCalendars handles multiple calendars`() {
        val response = """
            <?xml version="1.0"?>
            <d:multistatus xmlns:d="DAV:" xmlns:cal="urn:ietf:params:xml:ns:caldav">
              <d:response>
                <d:href>/remote.php/dav/calendars/user/calendar1/</d:href>
                <d:propstat>
                  <d:prop>
                    <d:resourcetype><cal:calendar/></d:resourcetype>
                    <d:displayname>Calendar 1</d:displayname>
                  </d:prop>
                </d:propstat>
              </d:response>
              <d:response>
                <d:href>/remote.php/dav/calendars/user/calendar2/</d:href>
                <d:propstat>
                  <d:prop>
                    <d:resourcetype><cal:calendar/></d:resourcetype>
                    <d:displayname>Calendar 2</d:displayname>
                  </d:prop>
                </d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val result = parser.parseCalendars(response, "https://cloud.school.nl")

        assertEquals(2, result.size)
    }

    // ==================== parseCtag tests ====================

    @Test
    fun `parseCtag extracts ctag from response`() {
        val response = """
            <?xml version="1.0"?>
            <d:multistatus xmlns:d="DAV:" xmlns:cs="http://calendarserver.org/ns/">
              <d:response>
                <d:propstat>
                  <d:prop>
                    <cs:getctag>abcd1234</cs:getctag>
                    <d:sync-token>token</d:sync-token>
                  </d:prop>
                </d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val result = parser.parseCtag(response)

        assertEquals("abcd1234", result)
    }

    @Test
    fun `parseCtag returns null for missing ctag`() {
        val response = """
            <?xml version="1.0"?>
            <d:multistatus xmlns:d="DAV:">
              <d:response>
                <d:propstat>
                  <d:prop>
                  </d:prop>
                </d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val result = parser.parseCtag(response)

        assertNull(result)
    }

    // ==================== parseSyncCollectionResponse tests ====================

    @Test
    fun `parseSyncCollectionResponse extracts sync token and changes`() {
        val response = """
            <?xml version="1.0"?>
            <d:multistatus xmlns:d="DAV:">
              <d:response>
                <d:href>/calendars/user/calendar/event1.ics</d:href>
                <d:propstat>
                  <d:prop>
                    <d:getetag>"abc123"</d:getetag>
                  </d:prop>
                  <d:status>HTTP/1.1 200 OK</d:status>
                </d:propstat>
              </d:response>
              <d:response>
                <d:href>/calendars/user/calendar/event2.ics</d:href>
                <d:propstat>
                  <d:status>HTTP/1.1 404 Not Found</d:status>
                </d:propstat>
              </d:response>
              <d:sync-token>newtoken456</d:sync-token>
            </d:multistatus>
        """.trimIndent()

        val result = parser.parseSyncCollectionResponse(response)

        assertEquals("newtoken456", result.syncToken)
        assertEquals(1, result.modified.size)
        assertEquals(1, result.deleted.size)
        assertTrue(result.modified.contains("/calendars/user/calendar/event1.ics"))
        assertTrue(result.deleted.contains("/calendars/user/calendar/event2.ics"))
    }

    // ==================== Helper method tests ====================

    @Test
    fun `normalizeColor handles standard hex color`() {
        val result = parser.normalizeColor("#0082c9")
        assertEquals("#0082c9", result)
    }

    @Test
    fun `normalizeColor handles color with alpha`() {
        val result = parser.normalizeColor("#0082c9FF")
        assertEquals("#0082c9", result)
    }

    @Test
    fun `normalizeColor returns null for invalid color`() {
        val result = parser.normalizeColor("invalid")
        assertNull(result)
    }

    @Test
    fun `normalizeColor returns null for null input`() {
        val result = parser.normalizeColor(null)
        assertNull(result)
    }

    @Test
    fun `resolveUrl handles absolute URL`() {
        val result = parser.resolveUrl("https://cloud.example.com", "https://other.com/path")
        assertEquals("https://other.com/path", result)
    }

    @Test
    fun `resolveUrl handles relative URL`() {
        val result = parser.resolveUrl("https://cloud.example.com", "/remote.php/dav/calendars/")
        assertEquals("https://cloud.example.com/remote.php/dav/calendars/", result)
    }
}
