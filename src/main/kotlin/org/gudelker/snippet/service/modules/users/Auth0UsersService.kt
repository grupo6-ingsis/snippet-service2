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
    ): Auth0UsersResponse =
        try {
            println("🔍 Auth0 Search - Query: '$query', Page: $page, PerPage: $perPage")

            // Search Query Construction
            val searchQuery =
                when {
                    query.isBlank() -> null
                    query.length == 1 -> "email:$query* OR name:$query* OR nickname:$query*"
                    query.length == 2 -> "email:$query* OR name:$query* OR nickname:$query*"
                    else -> "email:*$query* OR name:*$query* OR nickname:*$query*"
                }

            println("🔍 Auth0 Search Query: $searchQuery")

            val response =
                restClient.get()
                    .uri { uriBuilder ->
                        val builder =
                            uriBuilder
                                .scheme("https")
                                .host(domain)
                                .path("/api/v2/users")
                                .queryParam("page", page)
                                .queryParam("per_page", perPage.coerceAtMost(100))
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

            println("📦 Raw Auth0 Response: $response")

            if (response != null) {
                @Suppress("UNCHECKED_CAST")
                val usersList = response["users"] as? List<*>
                println("👥 Users list type: ${usersList?.javaClass}, size: ${usersList?.size}")

                val users =
                    usersList?.mapNotNull { userObj ->
                        try {
                            val userMap = userObj as? Map<String, Any>
                            if (userMap != null) {
                                Auth0User(
                                    user_id = userMap["user_id"] as? String ?: "",
                                    email = userMap["email"] as? String,
                                    name = userMap["name"] as? String,
                                    nickname = userMap["nickname"] as? String,
                                    picture = userMap["picture"] as? String,
                                    created_at = userMap["created_at"] as? String,
                                    updated_at = userMap["updated_at"] as? String,
                                )
                            } else {
                                println("⚠️ User object is not a Map: $userObj")
                                null
                            }
                        } catch (e: Exception) {
                            println("⚠️ Error parsing user: ${e.message}, userObj: $userObj")
                            null
                        }
                    } ?: emptyList()

                println("✅ Parsed ${users.size} users successfully")
                users.forEach { user ->
                    println("   - ${user.name} (${user.email})")
                }

                val total = (response["total"] as? Number)?.toInt() ?: users.size
                val start = (response["start"] as? Number)?.toInt() ?: 0
                val limit = (response["limit"] as? Number)?.toInt() ?: perPage

                println("📊 Total: $total, Start: $start, Limit: $limit")

                Auth0UsersResponse(
                    users = users,
                    total = total,
                    start = start,
                    limit = limit,
                )
            } else {
                println("⚠️ Auth0 returned null response")
                Auth0UsersResponse(emptyList(), 0, 0, perPage)
            }
        } catch (e: Exception) {
            println("❌ Error searching users in Auth0: ${e.message}")
            e.printStackTrace()
            Auth0UsersResponse(emptyList(), 0, 0, perPage)
        }
}
