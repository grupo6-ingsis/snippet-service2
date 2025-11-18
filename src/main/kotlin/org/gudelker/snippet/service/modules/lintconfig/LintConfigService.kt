package org.gudelker.snippet.service.modules.lintconfig

import org.gudelker.snippet.service.modules.lintconfig.input.ActivateRuleRequest
import org.gudelker.snippet.service.modules.lintrule.LintRuleRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class LintConfigService(
    private val lintConfigRepository: LintConfigRepository,
    private val lintRuleRepository: LintRuleRepository,
) {
    private val logger = LoggerFactory.getLogger(LintConfigService::class.java)

    fun modifyRule(
        request: ActivateRuleRequest,
        userId: String,
    ): LintConfig? {
        val ruleId = UUID.fromString(request.id)
        val existingConfig = lintConfigRepository.findByUserIdAndLintRuleId(userId, ruleId)

        logger.info("Modifying rule: userId=$userId, ruleId=$ruleId, isActive=${request.isActive}, existingConfig=${existingConfig?.id}")

        return when {
            // Activate new rule
            request.isActive && existingConfig == null -> {
                logger.info("Activating new rule: ${request.name}")
                val rule =
                    lintRuleRepository.findById(ruleId).orElseThrow {
                        ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found with id: $ruleId")
                    }
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
            // Update existing active rule
            request.isActive && existingConfig != null -> {
                logger.info("Updating existing rule: ${request.name}")
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
                logger.info("Deactivating rule: ${request.name}")
                lintConfigRepository.delete(existingConfig)
                null
            }
            // Rule already deactivated
            else -> {
                logger.info("No action needed for rule: ${request.name}")
                null
            }
        }
    }

    fun getAllRulesFromUser(userId: String): List<LintConfig> {
        return lintConfigRepository.findByUserId(userId)
    }
}
