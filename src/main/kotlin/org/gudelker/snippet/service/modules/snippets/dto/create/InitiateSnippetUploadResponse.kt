package org.gudelker.snippet.service.modules.snippets.dto.create

import java.util.UUID

data class InitiateSnippetUploadResponse(
    val snippetId: UUID,
    val uploadUrl: String,
    val expiresIn: Int,
    // Segundos hasta que expire la URL
)
