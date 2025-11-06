package org.gudelker.snippet.service.modules.snippets.dto.create

import jakarta.validation.constraints.NotBlank
import org.gudelker.snippet.service.modules.snippets.dto.Version

data class FinalizeSnippetRequest(
    @field:NotBlank
    val snippetId: String,
    @field:NotBlank
    val title: String,
    val description: String? = null,
    @field:NotBlank
    val language: String,
    val version: Version,
)
