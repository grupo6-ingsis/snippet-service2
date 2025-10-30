package org.gudelker.snippet.service.modules.lint_config

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID
@Repository
interface LintConfigRepository: JpaRepository<LintConfig, UUID> {
    fun findByUserId(userId: String): List<LintConfig>
    fun findByUserIdAndRuleId(userId: String, ruleId: String): LintConfig?
    fun findLintConfigsByUserIdAndEnabled(userId: String, enabled: Boolean): List<LintConfig>
}