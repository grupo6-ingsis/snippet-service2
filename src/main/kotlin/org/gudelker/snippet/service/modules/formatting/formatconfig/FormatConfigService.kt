package org.gudelker.snippet.service.modules.formatting.formatconfig

import org.gudelker.snippet.service.modules.formatting.formatconfig.input.ActivateFormatRuleRequest
import org.gudelker.snippet.service.modules.formatting.formatrule.FormatRuleRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class FormatConfigService(
    private val formatConfigRepository: FormatConfigRepository,
    private val formatRuleRepository: FormatRuleRepository,
) {
    fun modifyFormatConfig(
        request: ActivateFormatRuleRequest,
        userId: String,
    ): FormatConfig? {
        val ruleId = UUID.fromString(request.id)
        val existingConfig = formatConfigRepository.findByUserIdAndFormatRuleId(userId, ruleId)

        return when {
            request.isActive && existingConfig == null -> {
                val rule =
                    formatRuleRepository.findById(ruleId).orElseThrow {
                        ResponseStatusException(HttpStatus.NOT_FOUND, "Format rule not found with id: $ruleId")
                    }
                val config =
                    FormatConfig().apply {
                        this.userId = userId
                        this.formatRule = rule
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
                formatConfigRepository.save(config)
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
                formatConfigRepository.save(existingConfig)
            }
            !request.isActive && existingConfig != null -> {
                formatConfigRepository.delete(existingConfig)
                null
            }
            else -> null
        }
    }

    fun getAllRulesFromUser(userId: String): List<FormatConfig> {
        return formatConfigRepository.findByUserId(userId)
    }
}
