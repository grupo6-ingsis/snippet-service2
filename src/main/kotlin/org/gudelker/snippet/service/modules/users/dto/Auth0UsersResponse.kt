package org.gudelker.snippet.service.modules.users.dto

data class Auth0UsersResponse(
    val users: List<Auth0User>,
    val total: Int,
    val start: Int,
    val limit: Int,
)
