package org.gudelker.snippet.service.modules.snippets.input.update

data class UpdateSnippetFromEditorInput(
    val title: String? = null,
    val description: String? = null,
    val content: String? = null,
)
