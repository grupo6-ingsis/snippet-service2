package org.gudelker.snippet.service.modules.linting.lintrule

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/lintrule")
class LintRuleController(private val lintRuleRepository: LintRuleRepository) {
    private val logger = LoggerFactory.getLogger(LintRuleController::class.java)

    @GetMapping("/all")
    fun getAllLintRules(): List<LintRule> {
        logger.info("Fetching all available lint rules")
        val rules = lintRuleRepository.findAll()
        logger.info("Retrieved {} lint rules", rules.size)
        return rules
    }
}
