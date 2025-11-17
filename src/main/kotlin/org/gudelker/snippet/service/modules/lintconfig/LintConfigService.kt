package org.gudelker.snippet.service.modules.lintconfig

import org.gudelker.snippet.service.modules.lintconfig.input.ActivateRuleRequest
import org.gudelker.snippet.service.modules.lintrule.LintRuleRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class LintConfigService(
    private val lintConfigRepository: LintConfigRepository,
    private val lintRuleRepository: LintRuleRepository,
) {
    fun activateRule(
        request: ActivateRuleRequest,
        userId: String,
    ): LintConfig {
        val ruleId = UUID.fromString(request.id)
        val rule = lintRuleRepository.findById(ruleId).orElseThrow()
        val config =
            LintConfig().apply {
                this.userId = userId
                this.lintRule = rule
                this.ruleValue = request.ruleValue
            }
        return lintConfigRepository.save(config)
    }

    fun deactivateRule(
        userId: String,
        lintRuleId: String,
    ) {
        val ruleId = UUID.fromString(lintRuleId)
        val config =
            lintConfigRepository.findByUserIdAndLintRuleId(userId, ruleId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        lintConfigRepository.delete(config)
    }

    fun getAllRulesFromUser(userId: String): List<LintConfig> {
        return lintConfigRepository.findByUserId(userId)
    }
}
