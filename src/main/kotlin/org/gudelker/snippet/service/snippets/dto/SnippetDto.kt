package org.gudelker.snippet.service.snippets.dto

import jakarta.validation.constraints.NotBlank

class SnippetDto (
    @field:NotBlank
    val title: String,

    @field:NotBlank
    val content: String,

    @field:NotBlank
    val userId: String,
)