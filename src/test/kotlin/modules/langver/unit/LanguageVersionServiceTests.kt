package modules.langver.unit

import io.mockk.every
import io.mockk.mockk
import org.gudelker.snippet.service.modules.language.Language
import org.gudelker.snippet.service.modules.language.LanguageRepository
import org.gudelker.snippet.service.modules.langver.LanguageVersion
import org.gudelker.snippet.service.modules.langver.LanguageVersionController
import org.gudelker.snippet.service.modules.langver.LanguageVersionRepository
import org.gudelker.snippet.service.modules.langver.dto.LanguageVersionDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt

class LanguageVersionServiceTests {
    private val languageRepository = mockk<LanguageRepository>()
    private val languageVersionRepository = mockk<LanguageVersionRepository>()
    private val controller = LanguageVersionController(languageVersionRepository, languageRepository)
    private val jwt = mockk<Jwt>(relaxed = true)

    @Nested
    inner class GetSupportedLanguageVersions {
        @Test
        fun `returns supported versions for existing language`() {
            val language =
                Language().apply {
                    name = "PrintScript"
                    extension = "ps"
                }
            val version1 =
                LanguageVersion().apply {
                    version = "1.0"
                    this.language = language
                }
            val version2 =
                LanguageVersion().apply {
                    version = "1.1"
                    this.language = language
                }

            every { languageRepository.findByName("PrintScript") } returns language
            every { languageVersionRepository.findByLanguageName("PrintScript") } returns listOf(version1, version2)

            val result = controller.getSupportedLanguageVersions("PrintScript", jwt)

            assertEquals(
                LanguageVersionDto(
                    languageName = "PrintScript",
                    versions = listOf("1.0", "1.1"),
                    extension = "ps",
                ),
                result,
            )
        }

        @Test
        fun `throws exception if language not found`() {
            every { languageRepository.findByName("PrintScript") } returns null

            val ex =
                assertThrows(IllegalArgumentException::class.java) {
                    controller.getSupportedLanguageVersions("PrintScript", jwt)
                }
            assert(ex.message!!.contains("Language 'PrintScript' not found"))
        }
    }
}
