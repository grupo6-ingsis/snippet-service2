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
        if (lintRuleRepository.count() == 0L) {
            val rules =
                listOf(
                    LintRule().apply {
                        name = "identifierFormat"
                        description = "Ensures that identifiers follow a specific format, " +
                            "such as snake_case or camelCase."
                    },
                    LintRule().apply {
                        name = "restrictPrintlnToIdentifierOrLiteral"
                        description = "Restricts the use of `println` to only " +
                            "accept identifiers or string literals, preventing dynamic or unsafe expressions."
                    },
                    LintRule().apply {
                        name = "restrictReadInputToIdentifierOrLiteral"
                        description = "Restricts the use of `readInput` to only accept identifiers " +
                            "or string literals, ensuring controlled input handling."
                    },
                )
            lintRuleRepository.saveAll(rules)
        }
    }
}
