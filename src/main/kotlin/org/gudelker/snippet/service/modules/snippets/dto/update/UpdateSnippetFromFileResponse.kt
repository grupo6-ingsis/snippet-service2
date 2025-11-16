package org.gudelker.snippet.service.modules.snippets.dto.update

import org.gudelker.snippet.service.modules.language.Language
import java.time.OffsetDateTime

data class UpdateSnippetFromFileResponse(
    val snippetId: String,
    val title: String,
    val content: String,
    val language: Language,
    val updated: OffsetDateTime,
)
