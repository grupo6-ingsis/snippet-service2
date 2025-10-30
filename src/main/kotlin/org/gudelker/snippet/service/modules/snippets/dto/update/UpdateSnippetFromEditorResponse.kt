package org.gudelker.snippet.service.modules.snippets.dto.update

import java.time.OffsetDateTime

// Respuesta para update desde editor

data class UpdateSnippetFromEditorResponse(
    val snippetId: String,
    val title: String?,
    val description: String?,
    val content: String?,
    val language: String?,
    val version: String?,
    val updated: OffsetDateTime,
)

