package org.gudelker.snippet.service.modules.lintresult

import org.springframework.stereotype.Service

@Service
class LintResultService(
    private val lintResultRepository: LintResultRepository
) {
    fun snippetPassesRule(snippetId: String, lintRuleId: String): Boolean {
        val result = lintResultRepository.findAll().filter {
            it.snippet?.id.toString() == snippetId && it.lintRule?.id.toString() == lintRuleId
        }
        return result.all { it.passed }
    }
}