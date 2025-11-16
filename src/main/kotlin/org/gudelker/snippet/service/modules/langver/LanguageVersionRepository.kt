package org.gudelker.snippet.service.modules.langver

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface LanguageVersionRepository : JpaRepository<LanguageVersion, UUID> {
    fun findByLanguageName(languageName: String): List<LanguageVersion>

    fun findByLanguageNameAndVersion(
        languageName: String,
        version: String,
    ): LanguageVersion?
}
