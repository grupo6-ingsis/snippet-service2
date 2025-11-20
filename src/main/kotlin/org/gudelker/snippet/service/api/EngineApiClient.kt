package org.gudelker.snippet.service.api

import org.gudelker.snippet.service.auth.CachedTokenService
import org.gudelker.snippet.service.modules.interpret.InterpretSnippetRequest
import org.gudelker.snippet.service.modules.interpret.InterpretSnippetResponse
import org.gudelker.snippet.service.modules.snippets.dto.ParseSnippetRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class EngineApiClient(
    private val restClient: RestClient,
    private val cachedTokenService: CachedTokenService,
) {
    fun parseSnippet(request: ParseSnippetRequest): ResultType {
        val machineToken = cachedTokenService.getToken()
        return restClient.post()
            .uri("http://snippet-engine:8080/snippet/parse")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $machineToken")
            .body(request)
            .retrieve()
            .body(ResultType::class.java) ?: throw RuntimeException("No response from snippet service")
    }

    fun interpretSnippet(request: InterpretSnippetRequest): InterpretSnippetResponse {
        val machineToken = cachedTokenService.getToken()
        return restClient.post()
            .uri("http://snippet-engine:8080/snippet/interpret")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $machineToken")
            .body(request)
            .retrieve()
            .body(InterpretSnippetResponse::class.java) ?: throw RuntimeException("No response from snippet service")
    }
}
