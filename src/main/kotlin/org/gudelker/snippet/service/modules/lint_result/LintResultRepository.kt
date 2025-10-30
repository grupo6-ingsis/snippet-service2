package org.gudelker.snippet.service.modules.lint_result

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LintResultRepository: JpaRepository<LintResult, UUID> {
    fun findLintResultsBySnippetAndPassed(snippetId: UUID, passed: Boolean): List<LintResult>
}