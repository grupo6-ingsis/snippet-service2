package org.gudelker.snippet.service.redis

data class LintRequest(
    val snippetId: String,
    val ruleName: String,
    val requestedAt: Long = System.currentTimeMillis()
)
