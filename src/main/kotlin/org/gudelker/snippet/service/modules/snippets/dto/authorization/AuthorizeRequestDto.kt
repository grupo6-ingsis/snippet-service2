package org.gudelker.snippet.service.modules.snippets.dto.authorization

import org.gudelker.snippet.service.modules.snippets.dto.PermissionType

data class AuthorizeRequestDto(
    val userId: String,
    val permissions: List<PermissionType>,
)
