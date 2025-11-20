package org.gudelker.snippet.service.modules.users

import org.gudelker.snippet.service.auth.Auth0ManagementTokenService
import org.gudelker.snippet.service.modules.users.dto.Auth0UsersResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UsersController(
    private val auth0ManagementTokenService: Auth0ManagementTokenService,
    private val auth0UsersService: Auth0UsersService,
) {
    @GetMapping("/search")
    fun searchUsers(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(defaultValue = "") query: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") perPage: Int,
    ): ResponseEntity<Auth0UsersResponse> {
        return try {

            val managementToken = auth0ManagementTokenService.getManagementToken()
            val result =
                auth0UsersService.searchUsers(
                    managementToken = managementToken.access_token,
                    query = query,
                    page = page,
                    perPage = perPage.coerceAtMost(10),
                )

            ResponseEntity.ok(result)
        } catch (e: Exception) {
            println("❌ Error searching users: ${e.message}")
            e.printStackTrace()
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
}
