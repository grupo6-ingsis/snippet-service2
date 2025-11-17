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
    fun modifyRule(
        request: ActivateRuleRequest,
        userId: String,
    ): LintConfig? {
        val ruleId = UUID.fromString(request.id)
        val existingConfig = lintConfigRepository.findByUserIdAndLintRuleId(userId, ruleId)
        return when {
            request.isActive && existingConfig == null -> {
                val rule = lintRuleRepository.findById(ruleId).orElseThrow()
                val config =
                    LintConfig().apply {
                        this.userId = userId
                        this.lintRule = rule
                        this.ruleValue =
                            if (request.hasValue) {
                                request.ruleValue ?: throw ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "ruleValue cannot be null for rules that require a value",
                                )
                            } else {
                                null
                            }
                    }
                lintConfigRepository.save(config)
            }
            request.isActive && existingConfig != null -> {
                if (request.hasValue) {
                    if (request.ruleValue == null) {
                        throw ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "ruleValue cannot be null for rules that require a value",
                        )
                    }
                    existingConfig.ruleValue = request.ruleValue
                } else {
                    existingConfig.ruleValue = null
                }
                lintConfigRepository.save(existingConfig)
            }
            // Deactivate rule
            !request.isActive && existingConfig != null -> {
                lintConfigRepository.delete(existingConfig)
                existingConfig
            }
            else -> null
        }
    }

    fun getAllRulesFromUser(userId: String): List<LintConfig> {
        return lintConfigRepository.findByUserId(userId)
    }
}
