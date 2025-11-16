package org.gudelker.snippet.service.modules.language

import org.gudelker.snippet.service.modules.language.dto.LanguageWithExtensionDto
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller("language")
class LanguageController(
    private val languageRepository: LanguageRepository,
) {
    @GetMapping("/supported")
    fun getSupportedLanguages(): List<LanguageWithExtensionDto> {
        val languages = languageRepository.findAll()
        return languages.map { language ->
            LanguageWithExtensionDto(language.name, language.extension)
        }
    }
}
