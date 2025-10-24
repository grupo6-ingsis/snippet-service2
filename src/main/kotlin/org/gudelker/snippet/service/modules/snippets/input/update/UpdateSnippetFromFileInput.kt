package org.gudelker.snippet.service.modules.snippets.input.update

data class UpdateSnippetFromFileInput(
    val snippetId: String,
    val title: String?,
    val content: String?,
    val language: String?,
)
