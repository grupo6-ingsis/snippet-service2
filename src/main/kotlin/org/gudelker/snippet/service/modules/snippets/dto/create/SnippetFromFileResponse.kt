package org.gudelker.snippet.service.modules.snippets.dto.create

import jakarta.validation.constraints.NotBlank

class SnippetFromFileResponse(
    @field:NotBlank
    val title: String,
    @field:NotBlank
    val content: String,
    @field:NotBlank
    val userId: String,
)
