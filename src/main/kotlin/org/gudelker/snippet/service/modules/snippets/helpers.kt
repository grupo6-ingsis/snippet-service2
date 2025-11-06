package org.gudelker.snippet.service.modules.snippets

import org.gudelker.snippet.service.modules.lint_config.LintConfig
import org.gudelker.snippet.service.modules.lint_result.LintResultService

fun snippetPassesAllRules(
    snippet: Snippet,
    userLintRules: List<LintConfig>,
    lintResultService: LintResultService
): Boolean {
    return userLintRules.isEmpty() || userLintRules.all { lintConfig ->
        lintResultService.snippetPassesRule(
            snippet.id.toString(),
            lintConfig.lintRule?.id.toString()
        )
    }
}