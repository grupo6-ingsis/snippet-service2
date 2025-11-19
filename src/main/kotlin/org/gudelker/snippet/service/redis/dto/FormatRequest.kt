package org.gudelker.snippet.service.redis.dto

import org.gudelker.snippet.service.modules.snippets.dto.FormatRuleNameWithValue

data class FormatRequest(
    val snippetId: String,
    val snippetVersion: String,
    val userRules: List<FormatRuleNameWithValue>,
    val allRules: List<String>,
    val requestedAt: Long = System.currentTimeMillis(),
)
