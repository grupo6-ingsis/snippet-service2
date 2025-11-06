package org.gudelker.snippet.service.modules.snippets.input.create

import jakarta.validation.constraints.NotBlank

data class InitiateSnippetUploadInput(
        @field:NotBlank
        val filename: String, // Opcional: para validar extensiones
    )