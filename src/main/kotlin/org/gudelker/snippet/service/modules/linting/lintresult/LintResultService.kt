package org.gudelker.snippet.service.modules.linting.lintresult

import org.gudelker.snippet.service.modules.snippets.SnippetRepository
import org.gudelker.snippet.service.modules.snippets.dto.types.ComplianceType
import org.gudelker.snippet.service.redis.dto.LintResultRequest
import org.gudelker.snippet.service.redis.dto.SnippetIdWithLintResultsDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class LintResultService(
    private val lintResultRepository: LintResultRepository,
    private val snippetRepository: SnippetRepository,
) {
    @Transactional
    fun createOrUpdateLintResult(
        snippetId: String,
        results: List<LintResultRequest>,
    ): SnippetIdWithLintResultsDto {
        val snippet =
            snippetRepository.findById(UUID.fromString(snippetId))
                .orElseThrow { IllegalArgumentException("Snippet not found: $snippetId") }

        // Delete existing result if any
        lintResultRepository.deleteBySnippetId(snippet.id!!)

        // Create new result
        val lintResult =
            LintResult().apply {
                this.snippet = snippet
                this.complianceType =
                    if (results.isEmpty()) {
                        ComplianceType.COMPLIANT
                    } else {
                        ComplianceType.NON_COMPLIANT
                    }
                this.lintedAt = LocalDateTime.now()
                this.errors =
                    results.map { error ->
                        LintError(
                            message = error.message,
                            line = error.line.toInt(),
                            columnNumber = error.column.toInt(),
                        )
                    }.toMutableList()
            }

        lintResultRepository.save(lintResult)
        return SnippetIdWithLintResultsDto(snippetId, results)
    }

    fun getLintResultBySnippetId(snippetId: UUID): LintResult? {
        return lintResultRepository.findBySnippetId(snippetId)
    }

    fun getSnippetLintComplianceType(snippetId: UUID): ComplianceType? {
        val result = lintResultRepository.findBySnippetId(snippetId)
        return result?.complianceType
    }

    fun getSnippetLintErrors(snippetId: UUID): List<LintError> {
        val result = lintResultRepository.findBySnippetId(snippetId)
        return result?.errors ?: emptyList()
    }

    fun snippetPassesLinting(snippetId: String): Boolean {
        val result = lintResultRepository.findBySnippetId(UUID.fromString(snippetId))
        return result?.complianceType == ComplianceType.COMPLIANT || result?.complianceType == ComplianceType.PENDING
    }
}
