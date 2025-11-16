package org.gudelker.snippet.service.modules.langver

import org.gudelker.snippet.service.modules.language.LanguageRepository
import org.gudelker.snippet.service.modules.langver.dto.LanguageVersionDto
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
    ): List<LanguageVersionDto> {
        val language =
            languageRepository.findByName(languageName)
                ?: return emptyList()
        val versions = languageVersionRepository.findByLanguageName(languageName)
        return LanguageVersionDto(
            languageName = languageName,
            versions = versions.map { it.version },
            extension = language.extension,
        ).let {
            listOf(it)
        }
    }
}
