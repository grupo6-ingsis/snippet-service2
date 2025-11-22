package modules.auth0.unit

import io.mockk.every
import io.mockk.mockk
import org.gudelker.snippet.service.auth.Auth0ManagementTokenResponse
import org.gudelker.snippet.service.auth.Auth0ManagementTokenService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestClient

class Auth0ManagementTokenServiceTest {
    private val restClient = mockk<RestClient>(relaxed = true)
    private val clientId = "test-client-id"
    private val clientSecret = "test-client-secret"
    private val domain = "test-domain"
    private val tokenUrl = "https://test-domain/oauth/token"

    @Test
    fun `getManagementToken returns token when response is valid`() {
        val expectedResponse = Auth0ManagementTokenResponse("token123", "Bearer", 3600)
        val uriSpec = mockk<RestClient.RequestBodyUriSpec>(relaxed = true)
        val bodySpec = mockk<RestClient.RequestBodySpec>(relaxed = true)
        val responseSpec = mockk<RestClient.ResponseSpec>(relaxed = true)

        every { restClient.post() } returns uriSpec
        every { uriSpec.uri(tokenUrl) } returns bodySpec
        every { bodySpec.body(any<Map<String, String>>()) } returns bodySpec
        every { bodySpec.retrieve() } returns responseSpec
        every { responseSpec.body(Auth0ManagementTokenResponse::class.java) } returns expectedResponse

        val service = Auth0ManagementTokenService(restClient, clientId, clientSecret, domain, tokenUrl)
        val result = service.getManagementToken()
        Assertions.assertEquals(expectedResponse, result)
    }

    @Test
    fun `getManagementToken throws when response is null`() {
        val uriSpec = mockk<RestClient.RequestBodyUriSpec>(relaxed = true)
        val bodySpec = mockk<RestClient.RequestBodySpec>(relaxed = true)
        val responseSpec = mockk<RestClient.ResponseSpec>(relaxed = true)

        every { restClient.post() } returns uriSpec
        every { uriSpec.uri(tokenUrl) } returns bodySpec
        every { bodySpec.body(any<Map<String, String>>()) } returns bodySpec
        every { bodySpec.retrieve() } returns responseSpec
        every { responseSpec.body(Auth0ManagementTokenResponse::class.java) } returns null

        val service = Auth0ManagementTokenService(restClient, clientId, clientSecret, domain, tokenUrl)
        Assertions.assertThrows(RuntimeException::class.java) {
            service.getManagementToken()
        }
    }
}
