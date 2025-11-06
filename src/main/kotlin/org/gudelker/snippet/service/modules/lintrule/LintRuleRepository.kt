package org.gudelker.snippet.service.modules.lintrule

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LintRuleRepository : JpaRepository<LintRule, UUID> {
    fun findLintRulesById(id: UUID): LintRule?
    fun findLintRulesByName(name: String): List<LintRule>
}