package org.gudelker.snippet.service.modules.snippets.dto

data class AuthorizeRequestDto(
    val userId: String,
    val permissions: List<String>
)