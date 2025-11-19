package org.gudelker.snippet.service.modules.linting.lintrule

import org.springframework.stereotype.Service
import java.util.Optional
import java.util.UUID

@Service
class LintRuleService(
    private val lintRuleRepository: LintRuleRepository,
) {
    fun getLintRuleById(id: String): Optional<LintRule?> {
        return lintRuleRepository.findById(UUID.fromString(id))
    }

    fun getLintRulesByName(name: String): List<LintRule> {
        return lintRuleRepository.findByName(name)
    }

    fun getAllLintRules(): List<LintRule> {
        return lintRuleRepository.findAll()
    }
}
