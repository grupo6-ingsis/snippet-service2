package org.gudelker.snippet.service.auth

import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CachedTokenService(
    private val auth0TokenService: Auth0TokenService,
) {
    private var cachedToken: String? = null
    private var expiresAt: Instant? = null

    fun getToken(): String {
        if (cachedToken == null || Instant.now().isAfter(expiresAt)) {
            val tokenResponse = auth0TokenService.getMachineToMachineToken()

            cachedToken = tokenResponse.access_token
            expiresAt = Instant.now().plusSeconds(tokenResponse.expires_in.toLong() - 30)
        }

        return cachedToken!!
    }
}
