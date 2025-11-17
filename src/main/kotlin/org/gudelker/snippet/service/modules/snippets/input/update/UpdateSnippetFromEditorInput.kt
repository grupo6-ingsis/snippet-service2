package org.gudelker.snippet.service.modules.snippets.input.update

// Todos los campos opcionales excepto el id
// description se incluye porque está en el input de editor

data class UpdateSnippetFromEditorInput(
    val title: String? = null,
    val description: String? = null,
    val content: String? = null,
)
