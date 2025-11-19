package org.gudelker.snippet.service.modules.users

import org.gudelker.snippet.service.modules.users.dto.Auth0User
import org.gudelker.snippet.service.modules.users.dto.Auth0UsersResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class Auth0UsersService(
    private val restClient: RestClient,
    @Value("\${auth0.domain}") private val domain: String,
) {
    fun searchUsers(
        managementToken: String,
        query: String = "",
        page: Int = 0,
        perPage: Int = 10,
    ): Auth0UsersResponse {
        return try {
            val searchQuery = if (query.isNotBlank()) {
                "(email:*${query}* OR name:*${query}* OR nickname:*${query}*)"
            } else {
                null
            }

            val response = restClient.get()
                .uri { uriBuilder ->
                    val builder = uriBuilder
                        .scheme("https")
                        .host(domain)
                        .path("/api/v2/users")
                        .queryParam("page", page)
                        .queryParam("per_page", perPage.coerceAtMost(10))
                        .queryParam("include_totals", true)

                    if (searchQuery != null) {
                        builder
                            .queryParam("q", searchQuery)
                            .queryParam("search_engine", "v3")
                    }

                    builder.build()
                }
                .header(HttpHeaders.AUTHORIZATION, "Bearer $managementToken")
                .retrieve()
                .body(object : ParameterizedTypeReference<Map<String, Any>>() {})

            if (response != null) {
                @Suppress("UNCHECKED_CAST")
                val users = (response["users"] as? List<Map<String, Any>>)?.map { userMap ->
                    Auth0User(
                        user_id = userMap["user_id"] as? String ?: "",
                        email = userMap["email"] as? String,
                        name = userMap["name"] as? String,
                        nickname = userMap["nickname"] as? String,
                        picture = userMap["picture"] as? String,
                        created_at = userMap["created_at"] as? String,
                        updated_at = userMap["updated_at"] as? String,
                    )
                } ?: emptyList()

                val total = response["total"] as? Int ?: 0
                val start = response["start"] as? Int ?: 0
                val limit = response["limit"] as? Int ?: perPage

                Auth0UsersResponse(
                    users = users,
                    total = total,
                    start = start,
                    limit = limit,
                )
            } else {
                Auth0UsersResponse(emptyList(), 0, 0, perPage)
            }
        } catch (e: Exception) {
            println("Error searching users in Auth0: ${e.message}")
            e.printStackTrace()
            Auth0UsersResponse(emptyList(), 0, 0, perPage)
        }
    }
}