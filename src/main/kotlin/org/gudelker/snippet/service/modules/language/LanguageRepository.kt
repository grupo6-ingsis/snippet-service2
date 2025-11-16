package org.gudelker.snippet.service.modules.language

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface LanguageRepository : JpaRepository<Language, UUID> {
    fun findByName(name: String): Language?
}
