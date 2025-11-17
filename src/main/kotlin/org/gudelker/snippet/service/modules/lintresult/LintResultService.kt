package org.gudelker.snippet.service.modules.lintresult

import org.gudelker.snippet.service.modules.lintrule.LintRule
import org.gudelker.snippet.service.modules.snippets.Snippet
import org.gudelker.snippet.service.modules.snippets.dto.types.ComplianceType
import org.gudelker.snippet.service.redis.dto.LintResultRequest
import org.springframework.stereotype.Service

@Service
class LintResultService(
    private val lintResultRepository: LintResultRepository,
) {
    fun snippetPassesRule(
        snippetId: String,
        lintRuleId: String,
    ): Boolean {
        val result =
            lintResultRepository.findAll().filter {
                it.snippet?.id.toString() == snippetId && it.lintRule?.id.toString() == lintRuleId
            }
        return result.all { it.complianceType == ComplianceType.COMPLIANT }
    }

    fun createOrUpdateLintResult(snippet: Snippet, lintRule: LintRule, compl) {

    }
}
