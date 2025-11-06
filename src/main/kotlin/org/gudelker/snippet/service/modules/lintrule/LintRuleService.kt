package org.gudelker.snippet.service.modules.lintrule

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LintRuleService(
    private val lintRuleRepository: LintRuleRepository,
){
    fun getLintRuleById(id: String): LintRule? {
        return lintRuleRepository.findLintRulesById(UUID.fromString(id))
    }

    fun getLintRulesByName(name: String): List<LintRule> {
        return lintRuleRepository.findLintRulesByName(name)
    }

    fun getAllLintRules(): List<LintRule> {
        return lintRuleRepository.findAll()
    }
}