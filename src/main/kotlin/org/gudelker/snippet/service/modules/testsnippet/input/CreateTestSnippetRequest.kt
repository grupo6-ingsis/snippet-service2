package org.gudelker.snippet.service.modules.testsnippet.input

data class CreateTestSnippetRequest(
    val id: String,
    val snippetId: String,
    val name: String,
    val input: MutableList<String>?,
    val expectedOutput: MutableList<String>?,
)
