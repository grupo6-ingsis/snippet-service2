package org.gudelker.snippet.service.modules.snippets.input.update

import java.util.UUID

data class UpdateSnippetFromFileInput(
    val snippetId: UUID,
    val title: String?,
    val content: String?,
)
