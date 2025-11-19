package org.gudelker.snippet.service.modules.snippets.dto.get

data class SnippetContentDto(
    val content: String,
    val snippet: SnippetWithComplianceDto,
)
