package org.gudelker.snippet.service.modules.snippets.dto.share

import java.util.UUID

data class ShareSnippetResponseDto(
    val sharedUserId: String,
    val snippetId: UUID,
    val userId: String,
)
