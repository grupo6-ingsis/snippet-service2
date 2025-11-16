package org.gudelker.snippet.service.modules.snippets.input.create

data class CreateSnippetFromEditor(
    val title: String,
    val description: String,
    val language: String,
    val content: String,
    val version: String,
)
