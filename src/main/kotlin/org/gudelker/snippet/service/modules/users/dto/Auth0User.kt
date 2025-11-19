package org.gudelker.snippet.service.modules.users.dto

data class Auth0User(
    val user_id: String,
    val email: String?,
    val name: String?,
    val nickname: String?,
    val picture: String?,
    val created_at: String?,
    val updated_at: String?,
)
