package org.gudelker.snippet.service.modules.snippets.input

import jakarta.validation.constraints.NotBlank
import lombok.AllArgsConstructor

@AllArgsConstructor
data class CreateSnippetInput(

    @field:NotBlank
    val title: String,

    @field:NotBlank
    val content: String,

    @field:NotBlank
    val language: String
)

