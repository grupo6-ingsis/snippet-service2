package org.gudelker.snippet.service.modules.lintrule.seed

import jakarta.annotation.PostConstruct
import org.gudelker.snippet.service.modules.lintrule.LintRule
import org.gudelker.snippet.service.modules.lintrule.LintRuleRepository
import org.springframework.stereotype.Component

@Component
class LintRuleSeeder(
    private val lintRuleRepository: LintRuleRepository,
) {
    @PostConstruct
    fun seed() {
        val rulesConfig =
            listOf(
                Triple(
                    "identifierFormat",
                    "Ensures that identifiers follow a specific format, such as snake_case or camelCase.",
                    listOf("camelCase", "snakeCase"),
                ),
                Triple(
                    "restrictPrintlnToIdentifierOrLiteral",
                    "Restricts the use of `println` to only accept identifiers or string " +
                        "literals, preventing dynamic or unsafe expressions.",
                    emptyList(),
                ),
                Triple(
                    "restrictReadInputToIdentifierOrLiteral",
                    "Restricts the use of `readInput` to only accept identifiers or string literals, ensuring controlled input handling.",
                    emptyList(),
                ),
            )

        if (lintRuleRepository.count() == 0L) {
            // Create new rules
            val rules =
                rulesConfig.map { (name, description, options) ->
                    LintRule().apply {
                        this.name = name
                        this.description = description
                        this.hasValue = options.isNotEmpty()
                        this.valueOptions = options
                    }
                }
            lintRuleRepository.saveAll(rules)
        } else {
            // Update existing rules with valueOptions
            rulesConfig.forEach { (name, description, options) ->
                val existingRule = lintRuleRepository.findByName(name).firstOrNull()
                if (existingRule != null) {
                    existingRule.valueOptions = options
                    existingRule.description = description
                    existingRule.hasValue = options.isNotEmpty()
                    lintRuleRepository.save(existingRule)
                }
            }
        }
    }
}
