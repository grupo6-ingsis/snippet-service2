package org.gudelker.snippet.service.modules.langver

import org.gudelker.snippet.service.modules.language.LanguageRepository
import org.gudelker.snippet.service.modules.langver.dto.LanguageVersionDto
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
    @GetMapping("/supported")
    fun getSupportedLanguageVersions(
        @RequestParam languageName: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): LanguageVersionDto {
        val language =
            languageRepository.findByName(languageName)
                ?: throw IllegalArgumentException("Language '$languageName' not found")
        val versions = languageVersionRepository.findByLanguageName(languageName)
        return LanguageVersionDto(
            languageName = languageName,
            versions = versions.map { it.version },
            extension = language.extension,
        )
    }
}
