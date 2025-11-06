package org.gudelker.snippet.service.modules.lintconfig

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID
@Repository
interface LintConfigRepository: JpaRepository<LintConfig, UUID> {
    fun findByUserId(userId: String): List<LintConfig>
    fun findByUserIdAndLintRule(userId: String, ruleId: String): LintConfig?
}