package org.gudelker.snippet.service.modules.language

import org.gudelker.snippet.service.modules.langver.LanguageVersion
import org.gudelker.snippet.service.modules.langver.LanguageVersionRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import kotlin.io.extension

@Component
class LanguageSeeder(
    private val languageRepository: LanguageRepository,
    private val languageVersionRepository: LanguageVersionRepository,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val existingLanguage = languageRepository.findByName("PrintScript")
        if (existingLanguage != null) {
            return
        }

        val lang =
            Language().apply {
                name = "PrintScript"
                extension = "ps"
            }

        languageRepository.save(lang)

        val versions =
            listOf("1.0", "1.1").map { version ->
                LanguageVersion().apply {
                    this.version = version
                    this.language = lang
                }
            }

        languageVersionRepository.saveAll(versions)

        println("🌱 Seeded Language: PrintScript (.ps) with versions 1.0 and 1.1")
    }
}
