package modules.auth0.unit

import io.mockk.every
import io.mockk.mockk
import org.gudelker.snippet.service.auth.Auth0TokenResponse
import org.gudelker.snippet.service.auth.Auth0TokenService
import org.gudelker.snippet.service.auth.CachedTokenService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CachedTokenServiceTest {
    private val auth0TokenService = mockk<Auth0TokenService>()
    private val service = CachedTokenService(auth0TokenService)

    @Test
    fun `getToken returns cached token if not expired`() {
        val tokenResponse = Auth0TokenResponse("token123", "Bearer", 3600)
        every { auth0TokenService.getMachineToMachineToken() } returns tokenResponse
        val token1 = service.getToken()
        val token2 = service.getToken()
        Assertions.assertEquals(token1, token2)
        Assertions.assertEquals("token123", token1)
    }

    @Test
    fun `getToken fetches new token if expired`() {
        val tokenResponse1 = Auth0TokenResponse("token123", "Bearer", 1)
        val tokenResponse2 = Auth0TokenResponse("token456", "Bearer", 3600)
        every { auth0TokenService.getMachineToMachineToken() } returnsMany listOf(tokenResponse1, tokenResponse2)
        val token1 = service.getToken()
        // Simula expiración
        Thread.sleep(1100)
        val token2 = service.getToken()
        Assertions.assertEquals("token456", token2)
    }
}
