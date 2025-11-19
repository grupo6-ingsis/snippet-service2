package org.gudelker.snippet.service.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

data class Auth0ManagementTokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
)

@Service
class Auth0ManagementTokenService(
    private val restClient: RestClient,
    @Value("\${auth0.client-id}") private val clientId: String,
    @Value("\${auth0.client-secret}") private val clientSecret: String,
    @Value("\${auth0.domain}") private val domain: String,
    @Value("\${auth0.token-url}") private val tokenUrl: String,
) {
    fun getManagementToken(): Auth0ManagementTokenResponse {
        val requestBody = mapOf(
            "client_id" to clientId,
            "client_secret" to clientSecret,
            "audience" to "https://$domain/api/v2/",
            "grant_type" to "client_credentials",
        )

        val response = restClient.post()
            .uri(tokenUrl)
            .body(requestBody)
            .retrieve()
            .body(Auth0ManagementTokenResponse::class.java)

        return response ?: throw RuntimeException("Failed to obtain Auth0 Management API token")
    }
}