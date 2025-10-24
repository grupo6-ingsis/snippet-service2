package org.gudelker.snippet.service.modules.snippets.dto.authorization

data class AuthorizeRequestDto(
    val userId: String,
    val permissions: List<String>,
)
