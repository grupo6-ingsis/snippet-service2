package org.gudelker.snippet.service.modules.snippets.input.create

import org.gudelker.snippet.service.modules.snippets.dto.Version

data class CreateSnippetFromEditor(
    val title: String,
    val description: String,
    val language: String,
    val content: String,
    val version: Version,
)
