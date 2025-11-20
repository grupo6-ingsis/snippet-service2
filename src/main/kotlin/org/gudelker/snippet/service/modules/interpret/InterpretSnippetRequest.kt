package org.gudelker.snippet.service.modules.interpret

data class InterpretSnippetRequest(
    val snippetContent: String,
    val version: String,
    val inputs: MutableList<String>,
)
