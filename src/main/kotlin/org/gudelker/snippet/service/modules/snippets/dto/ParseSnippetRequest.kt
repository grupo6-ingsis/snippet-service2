package org.gudelker.snippet.service.modules.snippets.dto

data class ParseSnippetRequest(
    val snippetContent: String,
    val version: Version?,
)
