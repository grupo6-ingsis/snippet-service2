package modules.auth0.unit

import io.mockk.every
import io.mockk.mockk
import org.gudelker.snippet.service.auth.Auth0TokenResponse
import org.gudelker.snippet.service.auth.Auth0TokenService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestClient

class Auth0TokenServiceTest {
    private val restClient = mockk<RestClient>(relaxed = true)
    private val clientId = "test-client-id"
    private val clientSecret = "test-client-secret"
    private val audience = "test-audience"
    private val tokenUrl = "https://test-domain/oauth/token"

    @Test
    fun `getMachineToMachineToken returns token when response is valid`() {
        val expectedResponse = Auth0TokenResponse("token123", "Bearer", 3600)
        val uriSpec = mockk<RestClient.RequestBodyUriSpec>(relaxed = true)
        val bodySpec = mockk<RestClient.RequestBodySpec>(relaxed = true)
        val responseSpec = mockk<RestClient.ResponseSpec>(relaxed = true)

        every { restClient.post() } returns uriSpec
        every { uriSpec.uri(tokenUrl) } returns bodySpec
        every { bodySpec.body(any<Map<String, String>>()) } returns bodySpec
        every { bodySpec.retrieve() } returns responseSpec
        every { responseSpec.body(Auth0TokenResponse::class.java) } returns expectedResponse

        val service = Auth0TokenService(restClient, clientId, clientSecret, audience, tokenUrl)
        val result = service.getMachineToMachineToken()
        Assertions.assertEquals(expectedResponse, result)
    }

    @Test
    fun `getMachineToMachineToken throws when response is null`() {
        val uriSpec = mockk<RestClient.RequestBodyUriSpec>(relaxed = true)
        val bodySpec = mockk<RestClient.RequestBodySpec>(relaxed = true)
        val responseSpec = mockk<RestClient.ResponseSpec>(relaxed = true)

        every { restClient.post() } returns uriSpec
        every { uriSpec.uri(tokenUrl) } returns bodySpec
        every { bodySpec.body(any<Map<String, String>>()) } returns bodySpec
        every { bodySpec.retrieve() } returns responseSpec
        every { responseSpec.body(Auth0TokenResponse::class.java) } returns null

        val service = Auth0TokenService(restClient, clientId, clientSecret, audience, tokenUrl)
        Assertions.assertThrows(RuntimeException::class.java) {
            service.getMachineToMachineToken()
        }
    }
}
