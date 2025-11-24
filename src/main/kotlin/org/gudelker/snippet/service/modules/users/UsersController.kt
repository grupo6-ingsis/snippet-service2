package org.gudelker.snippet.service.modules.users

import org.gudelker.snippet.service.api.AuthApiClient
import org.gudelker.snippet.service.auth.Auth0ManagementTokenService
import org.gudelker.snippet.service.modules.users.dto.Auth0User
import org.gudelker.snippet.service.modules.users.dto.Auth0UsersResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UsersController(
    private val auth0ManagementTokenService: Auth0ManagementTokenService,
    private val auth0UsersService: Auth0UsersService,
    private val authApiClient: AuthApiClient,
) {
    private val logger = LoggerFactory.getLogger(UsersController::class.java)

    @GetMapping("/search")
    fun searchUsers(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(defaultValue = "") query: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") perPage: Int,
        @RequestParam(required = false) snippetId: String?,
    ): ResponseEntity<Auth0UsersResponse> {
        logger.info(
            "User: {} searching users - query: '{}', page: {}, perPage: {}, snippetId: {}",
            jwt.subject,
            query,
            page,
            perPage,
            snippetId ?: "none",
        )

        return try {
            val managementToken = auth0ManagementTokenService.getManagementToken()
            val result =
                auth0UsersService.searchUsers(
                    managementToken = managementToken.access_token,
                    query = query,
                    page = page,
                    perPage = perPage.coerceAtMost(10),
                )

            val filteredResult =
                if (snippetId != null) {
                    logger.debug("Filtering users with access to snippet: {}", snippetId)
                    val usersWithAccess = authApiClient.getUsersWithAccessToSnippet(snippetId)
                    logger.debug("Found {} users with existing access to snippet: {}", usersWithAccess.size, snippetId)

                    Auth0UsersResponse(
                        users =
                            result.users.filter { user ->
                                !usersWithAccess.contains(user.user_id)
                            },
                        total =
                            result.users.count { user ->
                                !usersWithAccess.contains(user.user_id)
                            },
                        start = result.start,
                        limit = result.limit,
                    )
                } else {
                    result
                }

            logger.info(
                "Successfully retrieved {} users for query: '{}' (total: {})",
                filteredResult.users.size,
                query,
                filteredResult.total,
            )
            ResponseEntity.ok(filteredResult)
        } catch (e: Exception) {
            logger.error("Error searching users for query: '{}' - Error: {}", query, e.message, e)
            ResponseEntity.status(500).body(
                Auth0UsersResponse(
                    users = emptyList(),
                    total = 0,
                    start = 0,
                    limit = perPage,
                ),
            )
        }
    }

    @GetMapping("/{userId}")
    fun getUserById(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable userId: String,
    ): ResponseEntity<Auth0User> {
        logger.info("User: {} requesting details for userId: {}", jwt.subject, userId)

        return try {
            val managementToken = auth0ManagementTokenService.getManagementToken()
            val user =
                auth0UsersService.getUserById(
                    managementToken = managementToken.access_token,
                    userId = userId,
                )

            if (user != null) {
                logger.info("Successfully retrieved user: {} (name: {})", userId, user.name)
                ResponseEntity.ok(user)
            } else {
                logger.warn("User not found: {}", userId)
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Error retrieving user: {} - Error: {}", userId, e.message, e)
            ResponseEntity.status(500).build()
        }
    }
}
