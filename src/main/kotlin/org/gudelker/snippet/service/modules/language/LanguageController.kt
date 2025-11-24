package org.gudelker.snippet.service.modules.language

import org.gudelker.snippet.service.modules.language.dto.LanguageWithExtensionDto
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/language"])
class LanguageController(
    private val languageRepository: LanguageRepository,
) {
    private val logger = LoggerFactory.getLogger(LanguageController::class.java)

    @GetMapping("/supported")
    fun getSupportedLanguages(
        @AuthenticationPrincipal jwt: Jwt,
    ): List<LanguageWithExtensionDto> {
        logger.info("Fetching supported languages for user: {}", jwt.subject)
        val languages = languageRepository.findAll()
        val result =
            languages.map {
                LanguageWithExtensionDto(
                    language = it.name,
                    extension = it.extension,
                )
            }
        logger.info("Retrieved {} supported languages", result.size)
        return result
    }
}
