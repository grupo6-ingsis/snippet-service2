package org.gudelker.snippet.service.modules.langver

import org.gudelker.snippet.service.modules.language.LanguageRepository
import org.gudelker.snippet.service.modules.langver.dto.LanguageVersionDto
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/language-version")
class LanguageVersionController(
    private val languageVersionRepository: LanguageVersionRepository,
    private val languageRepository: LanguageRepository,
) {
    private val logger = LoggerFactory.getLogger(LanguageVersionController::class.java)

    @GetMapping("/supported")
    fun getSupportedLanguageVersions(
        @RequestParam languageName: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): LanguageVersionDto {
        logger.info("Fetching versions for language: {} requested by user: {}", languageName, jwt.subject)

        return try {
            val language =
                languageRepository.findByName(languageName)
                    ?: throw IllegalArgumentException("Language '$languageName' not found")

            val versions = languageVersionRepository.findByLanguageName(languageName)
            logger.info("Found {} versions for language: {}", versions.size, languageName)

            LanguageVersionDto(
                languageName = languageName,
                versions = versions.map { it.version },
                extension = language.extension,
            )
        } catch (e: IllegalArgumentException) {
            logger.warn("Language not found: {} - Error: {}", languageName, e.message)
            throw e
        } catch (e: Exception) {
            logger.error("Error fetching versions for language: {} - Error: {}", languageName, e.message, e)
            throw e
        }
    }
}
