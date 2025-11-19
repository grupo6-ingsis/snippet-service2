package org.gudelker.snippet.service.modules.linting.lintresult

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LintResultRepository : JpaRepository<LintResult, UUID> {
    fun findBySnippetId(snippetId: UUID): LintResult?

    @Modifying
    fun deleteBySnippetId(snippetId: UUID)
}
