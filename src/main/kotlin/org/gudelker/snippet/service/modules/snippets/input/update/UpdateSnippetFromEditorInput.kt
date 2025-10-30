package org.gudelker.snippet.service.modules.snippets.input.update

import org.gudelker.snippet.service.modules.snippets.dto.Version

// Todos los campos opcionales excepto el id
// description se incluye porque está en el input de editor

data class UpdateSnippetFromEditorInput(
    val snippetId: String,
    val title: String? = null,
    val description: String? = null,
    val language: String? = null,
    val content: String? = null,
    val version: Version? = null
)

