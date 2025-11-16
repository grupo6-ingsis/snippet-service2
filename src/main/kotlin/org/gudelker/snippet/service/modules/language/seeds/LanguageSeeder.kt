package org.gudelker.snippet.service.modules.language

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class LanguageSeeder(
    private val languageRepository: LanguageRepository,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        if (languageRepository.findByName("PrintScript") != null) {
            return
        }

        val lang =
            Language().apply {
                name = "PrintScript"
                extension = "ps"
            }

        languageRepository.save(lang)
        println("🌱 Seeded Language: PrintScript (.ps)")
    }
}
