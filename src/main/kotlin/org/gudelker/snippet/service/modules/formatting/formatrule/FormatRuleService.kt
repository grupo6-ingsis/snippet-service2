package org.gudelker.snippet.service.modules.formatting.formatrule
import org.springframework.stereotype.Service
import java.util.Optional
import java.util.UUID

@Service
class FormatRuleService(private val formatRuleRepository: FormatRuleRepository) {
    fun getFormatRuleById(id: String): Optional<FormatRule?> {
        return formatRuleRepository.findById(UUID.fromString(id))
    }

    fun getFormatRulesByName(name: String): List<FormatRule> {
        return formatRuleRepository.findByName(name)
    }

    fun getAllFormatRules(): List<FormatRule> {
        return formatRuleRepository.findAll()
    }
}
