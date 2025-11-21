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
        try {
            // Construir el query de búsqueda
            val searchQuery =
                if (query.isBlank()) {
                    null
                } else {
                    "email:$query* OR name:$query* OR nickname:$query*"
                }

            // Intentar primero con el tipo genérico Map
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
                            builder.queryParam("q", searchQuery)
                            builder.queryParam("search_engine", "v3")
                        }

                        builder.build()
                    }
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $managementToken")
                    .retrieve()
                    .body(object : ParameterizedTypeReference<Map<String, Any>>() {})

            if (response == null) {
                println("⚠️ Auth0 returned null response")
                return Auth0UsersResponse(emptyList(), 0, 0, perPage)
            }

            // Extraer total primero
            val total =
                when (val totalValue = response["total"]) {
                    is Number -> totalValue.toInt()
                    is String -> totalValue.toIntOrNull() ?: 0
                    else -> {
                        println("⚠️ Total value is unexpected type: ${totalValue?.javaClass}")
                        0
                    }
                }
            // Extraer start
            val start =
                when (val startValue = response["start"]) {
                    is Number -> startValue.toInt()
                    is String -> startValue.toIntOrNull() ?: 0
                    else -> 0
                }

            // Extraer limit
            val limit =
                when (val limitValue = response["limit"]) {
                    is Number -> limitValue.toInt()
                    is String -> limitValue.toIntOrNull() ?: perPage
                    else -> perPage
                }

            // Procesar la lista de usuarios
            val usersValue = response["users"]
            val users =
                when (usersValue) {
                    is List<*> -> {
                        usersValue.mapNotNull { userObj ->
                            try {
                                when (userObj) {
                                    is Map<*, *> -> {
                                        @Suppress("UNCHECKED_CAST")
                                        val userMap = userObj as Map<String, Any>

                                        val userId = userMap["user_id"]?.toString() ?: ""
                                        val email = userMap["email"]?.toString()
                                        val name = userMap["name"]?.toString()
                                        val nickname = userMap["nickname"]?.toString()
                                        val picture = userMap["picture"]?.toString()
                                        val createdAt = userMap["created_at"]?.toString()
                                        val updatedAt = userMap["updated_at"]?.toString()

                                        Auth0User(
                                            user_id = userId,
                                            email = email,
                                            name = name,
                                            nickname = nickname,
                                            picture = picture,
                                            created_at = createdAt,
                                            updated_at = updatedAt,
                                        )
                                    }
                                    else -> {
                                        println("⚠️ User object is not a Map: $userObj")
                                        null
                                    }
                                }
                            } catch (e: Exception) {
                                println("❌ Error parsing user: ${e.message}")
                                e.printStackTrace()
                                null
                            }
                        }
                    }
                    else -> {
                        println("❌ Users is not a List! Type: ${usersValue?.javaClass}")
                        emptyList()
                    }
                }

            users.forEachIndexed { index, user ->
                println("   [$index] ${user.name} (${user.email}) - ID: ${user.user_id}")
            }

            return Auth0UsersResponse(
                users = users,
                total = total,
                start = start,
                limit = limit,
            )
        } catch (e: Exception) {
            println("❌ Error searching users in Auth0: ${e.message}")
            e.printStackTrace()
            return Auth0UsersResponse(emptyList(), 0, 0, perPage)
        }
    }

    fun getUserById(
        managementToken: String,
        userId: String,
    ): Auth0User? {
        return try {
            val response =
                restClient.get()
                    .uri { uriBuilder ->
                        uriBuilder
                            .scheme("https")
                            .host(domain)
                            .path("/api/v2/users/{userId}")
                            .build(userId)
                    }
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $managementToken")
                    .retrieve()
                    .body(object : ParameterizedTypeReference<Map<String, Any>>() {})

            if (response == null) {
                println("⚠️ Auth0 returned null for user $userId")
                return null
            }

            Auth0User(
                user_id = response["user_id"]?.toString() ?: userId,
                email = response["email"]?.toString(),
                name = response["name"]?.toString(),
                nickname = response["nickname"]?.toString(),
                picture = response["picture"]?.toString(),
                created_at = response["created_at"]?.toString(),
                updated_at = response["updated_at"]?.toString(),
            )
        } catch (e: Exception) {
            println("❌ Error fetching user $userId from Auth0: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
