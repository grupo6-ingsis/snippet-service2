package org.gudelker.snippet.service.redis.dto

data class SnippetIdWithLintResultsDto(
    val snippetId: String,
    val results: List<LintResultRequest>,
)
