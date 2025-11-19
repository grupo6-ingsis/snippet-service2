package org.gudelker.snippet.service.modules.formatting.formatrule.seed

import jakarta.annotation.PostConstruct
import org.gudelker.snippet.service.modules.formatting.formatrule.FormatRule
import org.gudelker.snippet.service.modules.formatting.formatrule.FormatRuleRepository
import org.springframework.stereotype.Component

@Component
class FormatRuleSeeder(
    private val formatRuleRepository: FormatRuleRepository,
) {
    @PostConstruct
    fun seed() {
        val rulesConfig =
            listOf(
                Triple(
                    "enforce-spacing-after-colon-in-declaration",
                    "Controls the number of spaces required after a colon in variable or function declarations.",
                    listOf(0, 1, 2),
                ),
                Triple(
                    "enforce-spacing-before-colon-in-declaration",
                    "Controls the number of spaces required before a colon in variable or function declarations.",
                    listOf(0, 1, 2),
                ),
                Triple(
                    "enforce-spacing-around-equals",
                    "Specifies the number of spaces required around the equals sign in assignments or default values.",
                    listOf(0, 1, 2),
                ),
                Triple(
                    "line-breaks-after-println",
                    "Defines how many line breaks must follow a println statement.",
                    listOf(0, 1, 2),
                ),
                Triple(
                    "indent-inside-if",
                    "Sets the indentation level for code blocks inside an if statement.",
                    listOf(0, 1, 2),
                ),
                Triple(
                    "if-brace-same-line",
                    "Determines if the opening brace of an if statement should be on the same line as the condition.",
                    emptyList(),
                ),
                Triple(
                    "mandatory-line-break-after-statement",
                    "Specifies the number of mandatory line breaks required after each statement.",
                    listOf(0, 1, 2),
                ),
            )

        if (formatRuleRepository.count() == 0L) {
            val rules =
                rulesConfig.map { (name, description, options) ->
                    FormatRule().apply {
                        this.name = name
                        this.description = description
                        this.hasValue = options.isNotEmpty()
                        this.valueOptions = options
                    }
                }
            formatRuleRepository.saveAll(rules)
        } else {
            rulesConfig.forEach { (name, description, options) ->
                val existingRule = formatRuleRepository.findAll().find { it.name == name }
                if (existingRule != null) {
                    existingRule.valueOptions = options
                    existingRule.description = description
                    existingRule.hasValue = options.isNotEmpty()
                    formatRuleRepository.save(existingRule)
                }
            }
        }
    }
}
