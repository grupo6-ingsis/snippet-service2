package org.gudelker.snippet.service.modules.formatting.formatrule

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FormatRuleRepository : JpaRepository<FormatRule, UUID> {
    fun findByName(name: String): List<FormatRule>

    fun existsByName(name: String): Boolean
}
