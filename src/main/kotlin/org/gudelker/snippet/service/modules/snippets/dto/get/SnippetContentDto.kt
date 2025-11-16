package org.gudelker.snippet.service.modules.snippets.dto.get

import org.gudelker.snippet.service.modules.snippets.Snippet

data class SnippetContentDto(
    val content: String,
    val snippet: Snippet,
)
