package org.gudelker.snippet.service.redis.dto

import org.gudelker.snippet.service.modules.snippets.dto.RuleNameWithValue

data class LintRequest(
    val snippetId: String,
    val snippetVersion: String,
    val userRules: List<RuleNameWithValue>,
    val allRules: List<String>,
    val requestedAt: Long = System.currentTimeMillis(),
)
