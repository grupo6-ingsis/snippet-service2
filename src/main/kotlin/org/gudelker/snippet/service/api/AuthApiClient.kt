package org.gudelker.snippet.service.api

import org.gudelker.snippet.service.auth.CachedTokenService
import org.gudelker.snippet.service.modules.snippets.dto.ParseSnippetRequest
import org.gudelker.snippet.service.modules.snippets.dto.authorization.AuthorizeRequestDto
import org.gudelker.snippet.service.modules.snippets.dto.authorization.AuthorizeResponseDto
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.util.UUID

@Service
class AuthApiClient(
    private val restClient: RestClient,
    private val cachedTokenService: CachedTokenService,
) {
    fun authorizeSnippet(
        snippetId: UUID,
        request: AuthorizeRequestDto,
    ): AuthorizeResponseDto {
        val machineToken = cachedTokenService.getToken()
        println(machineToken)
        return restClient.post()
            .uri("http://authorization-api:8080/api/permissions/authorize/$snippetId")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $machineToken") // M2M token
            .body(request)
            .retrieve()
            .body(AuthorizeResponseDto::class.java)
            ?: throw RuntimeException("No response from authorization service")
    }

    fun authorizeUpdateSnippet(snippetId: String): Boolean {
        val machineToken = cachedTokenService.getToken()

        return restClient.get()
            .uri("http://authorization-api:8080/api/permissions/authorize-update/$snippetId")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $machineToken")
            .retrieve()
            .body(Boolean::class.java)
            ?: throw RuntimeException("No response from authorization service")
    }

    fun parseSnippet(request: ParseSnippetRequest): ResultType {
        val machineToken = cachedTokenService.getToken()
        return restClient.post()
            .uri("http://snippet-engine-api:8080/snippet/parse")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $machineToken")
            .body(request)
            .retrieve()
            .body(ResultType::class.java) ?: throw RuntimeException("No response from snippet service")
    }

    fun isUserAuthorizedToWriteSnippet(
        snippetId: String,
        userId: String,
    ): Boolean {
        val machineToken = cachedTokenService.getToken()

        return restClient.post()
            .uri("http://authorization-api:8080/api/permissions/can-write/$snippetId")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $machineToken")
            .header("X-User-Id", userId)
            .retrieve()
            .body(Boolean::class.java)
            ?: false
    }
}
