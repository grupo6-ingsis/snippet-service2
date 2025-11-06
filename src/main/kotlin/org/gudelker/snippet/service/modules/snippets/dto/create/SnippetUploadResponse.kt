package org.gudelker.snippet.service.modules.snippets.dto.create

import org.gudelker.snippet.service.modules.snippets.Snippet

data class SnippetUploadResponse(
    val success: Boolean,
    val message: String,
    val snippetId: String?,
    val snippet: Snippet? = null,
)
