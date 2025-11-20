package org.gudelker.snippet.service.modules.testsnippet.dto

data class TestSnippetResponseDto(
    val name: String,
    val input: List<String>?,
    val output: List<String>?,
    val snippetId: String?,
)
