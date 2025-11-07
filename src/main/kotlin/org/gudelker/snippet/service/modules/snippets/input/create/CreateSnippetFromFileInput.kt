package org.gudelker.snippet.service.modules.snippets.input.create

import jakarta.validation.constraints.NotBlank
import org.gudelker.snippet.service.modules.snippets.dto.Version

data class CreateSnippetFromFileInput(
    @field:NotBlank
    val title: String,
    @field:NotBlank
    val content: String,
    @field:NotBlank
    val language: String,
    val version: Version,
    val description: String,
)
