package org.gudelker.snippet.service.modules.formatting.formatrule
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/formatrule")
class FormatRuleController(private val formatRuleRepository: FormatRuleRepository) {
    @GetMapping("/all")
    fun getAllFormatRules(): List<FormatRule> {
        return formatRuleRepository.findAll()
    }
}
