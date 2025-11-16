package org.gudelker.snippet.service.modules.language

import org.gudelker.snippet.service.modules.language.dto.LanguageWithExtensionDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/language"])
class LanguageController(
    private val languageRepository: LanguageRepository,
) {
    @GetMapping("/supported")
    fun getSupportedLanguages(): List<LanguageWithExtensionDto> {
        val languages = languageRepository.findAll()
        return languages.map {
            LanguageWithExtensionDto(
                language = it.name,
                extension = it.extension,
            )
        }
    }
}
