package org.gudelker.snippet.service.modules.snippets.dto.update

import org.gudelker.snippet.service.modules.language.Language
import java.time.OffsetDateTime

// Respuesta para update desde editor

data class UpdateSnippetFromEditorResponse(
    val snippetId: String,
    val title: String?,
    val description: String?,
    val content: String?,
    val language: Language,
    val version: String?,
    val updated: OffsetDateTime,
)
