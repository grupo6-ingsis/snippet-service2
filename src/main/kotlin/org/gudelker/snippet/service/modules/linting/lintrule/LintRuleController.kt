package org.gudelker.snippet.service.modules.linting.lintrule

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/lintrule")
class LintRuleController(private val lintRuleRepository: LintRuleRepository) {
    @GetMapping("/all")
    fun getAllLintRules(): List<LintRule> {
        return lintRuleRepository.findAll()
    }
}
