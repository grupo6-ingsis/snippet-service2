package org.gudelker.snippet.service.modules.interpret

import org.gudelker.snippet.service.api.ResultType

data class InterpretSnippetResponse(
    val results: ArrayList<String>,
    val resultType: ResultType,
)
