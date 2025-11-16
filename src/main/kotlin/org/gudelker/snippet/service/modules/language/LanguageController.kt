package org.gudelker.snippet.service.modules.language

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/language"])
class LanguageController(
    private val languageRepository: LanguageRepository,
) {
    @GetMapping("/supported")
    fun getSupportedLanguages(): List<String> {
        val languages = languageRepository.findAll()
        return languages.map { it.name }
    }
}
