package modules.language.unit

import org.gudelker.snippet.service.modules.language.Language
import org.gudelker.snippet.service.modules.language.dto.LanguageWithExtensionDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LanguageTests {
    @Test
    fun `should create Language with seed id and valid values`() {
        val lang =
            Language().apply {
                name = "PrintScript"
                extension = "ps"
            }
        assertEquals("PrintScript", lang.name)
        assertEquals("ps", lang.extension)
        assertNull(lang.id)
    }

    @Test
    fun `should allow empty name and extension`() {
        val lang = Language()
        assertEquals("", lang.name)
        assertEquals("", lang.extension)
    }

    @Test
    fun `should handle special characters in name and extension`() {
        val lang =
            Language().apply {
                name = "L@nguage#123"
                extension = "ext!@#"
            }
        assertEquals("L@nguage#123", lang.name)
        assertEquals("ext!@#", lang.extension)
    }

    @Test
    fun `should not allow id to be set manually`() {
        val lang = Language()
        assertNull(lang.id)
    }

    @Test
    fun `should create LanguageWithExtensionDto with valid values`() {
        val dto = LanguageWithExtensionDto(language = "PrintScript", extension = "ps")
        assertEquals("PrintScript", dto.language)
        assertEquals("ps", dto.extension)
    }

    @Test
    fun `should allow empty language and extension in LanguageWithExtensionDto`() {
        val dto = LanguageWithExtensionDto(language = "", extension = "")
        assertEquals("", dto.language)
        assertEquals("", dto.extension)
    }

    @Test
    fun `should handle long and special characters in LanguageWithExtensionDto`() {
        val dto = LanguageWithExtensionDto(language = "L@nguage#123", extension = "ext!@#")
        assertEquals("L@nguage#123", dto.language)
        assertEquals("ext!@#", dto.extension)
    }
}
