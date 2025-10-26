package org.gudelker.snippet.service.api

import org.gudelker.snippet.service.auth.CachedTokenService
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
        userToken: String,
    ): AuthorizeResponseDto {
        val machineToken = cachedTokenService.getToken()

        return restClient.post()
            .uri("http://localhost:8080/authorize/$snippetId")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $machineToken") // M2M token
            .body(request)
            .retrieve()
            .body(AuthorizeResponseDto::class.java)
            ?: throw RuntimeException("No response from authorization service")
    }

    fun authorizeUpdateSnippet(
        snippetId: String,
        userToken: String,
    ): Boolean {
        val machineToken = cachedTokenService.getToken()

        return restClient.get()
            .uri("http://authorization-service-api:8080/authorize-update/$snippetId")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $machineToken")
            .retrieve()
            .body(Boolean::class.java)
            ?: throw RuntimeException("No response from authorization service")
    }
}
