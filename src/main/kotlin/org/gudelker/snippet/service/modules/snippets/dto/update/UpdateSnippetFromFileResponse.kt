package org.gudelker.snippet.service.modules.snippets.dto.update

import java.time.OffsetDateTime

data class UpdateSnippetFromFileResponse(
    val snippetId: String,
    val title: String,
    val content: String,
    val language: String,
    val version: String,
    val updated: OffsetDateTime,
)
