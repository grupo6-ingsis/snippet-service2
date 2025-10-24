package org.gudelker.snippet.service.modules.snippets.dto

data class AuthorizeResponseDto(
    val success: Boolean,
    val message: String,
    val permissions: List<String>? = null
)