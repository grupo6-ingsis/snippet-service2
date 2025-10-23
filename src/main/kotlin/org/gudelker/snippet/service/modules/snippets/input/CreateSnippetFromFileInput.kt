package org.gudelker.snippet.service.modules.snippets.input

import jakarta.validation.constraints.NotBlank
import lombok.AllArgsConstructor

@AllArgsConstructor
data class CreateSnippetFromFileInput(

    @field:NotBlank
    val title: String,

    @field:NotBlank
    val description: String,

    @field:NotBlank
    val language: String,

    val permissionNames: Set<String>? = null
)

