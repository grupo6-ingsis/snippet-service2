package org.gudelker.snippet.service.modules.snippets.dto.share

import org.gudelker.snippet.service.modules.snippets.dto.PermissionType
import java.util.UUID

data class ShareSnippetResponseDto(
    val sharedUserId: String,
    val snippetId: UUID,
    val userId: String,
    val permissionType: PermissionType,
)
