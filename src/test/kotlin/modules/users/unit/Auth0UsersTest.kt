package modules.users.unit

import io.mockk.every
import io.mockk.mockk
import org.gudelker.snippet.service.modules.users.Auth0UsersService
import org.gudelker.snippet.service.modules.users.dto.Auth0User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClient.RequestHeadersUriSpec
import org.springframework.web.util.UriBuilder
import java.util.UUID
import java.util.function.Function
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class Auth0UsersTest {
    private lateinit var restClient: RestClient
    private lateinit var auth0UsersService: Auth0UsersService
    private val domain = "test.auth0.com"

    @BeforeEach
    fun setUp() {
        restClient = mockk(relaxed = true)
        auth0UsersService = Auth0UsersService(restClient, domain)
    }

    @Nested
    inner class SearchUsersTests {
        @Test
        fun `should return users when response is valid`() {
            val user =
                Auth0User(
                    user_id = UUID.randomUUID().toString(),
                    email = "test@example.com",
                    name = "Test User",
                    nickname = "testuser",
                    picture = null,
                    created_at = null,
                    updated_at = null,
                )
            val responseMap =
                mapOf(
                    "users" to
                        listOf(
                            mapOf(
                                "user_id" to user.user_id,
                                "email" to user.email,
                                "name" to user.name,
                                "nickname" to user.nickname,
                            ),
                        ),
                    "total" to 1,
                    "start" to 0,
                    "limit" to 10,
                )
            val uriSpec = mockk<RequestHeadersUriSpec<*>>()
            val getSpec = mockk<RestClient.RequestHeadersSpec<*>>()
            val retrieveSpec = mockk<RestClient.ResponseSpec>()
            every { restClient.get() } returns uriSpec
            every { uriSpec.uri(any<Function<UriBuilder, java.net.URI>>()) } returns getSpec
            every { getSpec.header(any(), any()) } returns getSpec
            every { getSpec.retrieve() } returns retrieveSpec
            every { retrieveSpec.body(any<ParameterizedTypeReference<Map<String, Any>>>()) } returns responseMap

            val result = auth0UsersService.searchUsers("token", "test", 0, 10)
            assertEquals(1, result.users.size)
            assertEquals(user.email, result.users[0].email)
            assertEquals(1, result.total)
            assertEquals(0, result.start)
            assertEquals(10, result.limit)
        }

        @Test
        fun `should return empty response if Auth0 returns null`() {
            val uriSpec = mockk<RequestHeadersUriSpec<*>>()
            val getSpec = mockk<RestClient.RequestHeadersSpec<*>>()
            val retrieveSpec = mockk<RestClient.ResponseSpec>()
            every { restClient.get() } returns uriSpec
            every { uriSpec.uri(any<Function<UriBuilder, java.net.URI>>()) } returns getSpec
            every { getSpec.header(any(), any()) } returns getSpec
            every { getSpec.retrieve() } returns retrieveSpec
            every { retrieveSpec.body(any<ParameterizedTypeReference<Map<String, Any>>>()) } returns null

            val result = auth0UsersService.searchUsers("token", "", 0, 10)
            assertTrue(result.users.isEmpty())
            assertEquals(0, result.total)
        }

        @Test
        fun `should handle exception and return empty response`() {
            val uriSpec = mockk<RequestHeadersUriSpec<*>>()
            val getSpec = mockk<RestClient.RequestHeadersSpec<*>>()
            val retrieveSpec = mockk<RestClient.ResponseSpec>()
            every { restClient.get() } returns uriSpec
            every { uriSpec.uri(any<Function<UriBuilder, java.net.URI>>()) } returns getSpec
            every { getSpec.header(any(), any()) } returns getSpec
            every { getSpec.retrieve() } returns retrieveSpec
            every { retrieveSpec.body(any<ParameterizedTypeReference<Map<String, Any>>>()) } throws RuntimeException("fail")

            val result = auth0UsersService.searchUsers("token", "", 0, 10)
            assertTrue(result.users.isEmpty())
            assertEquals(0, result.total)
        }
    }

    @Nested
    inner class GetUserByIdTests {
        @Test
        fun `should return user when found`() {
            val userId = UUID.randomUUID().toString()
            val responseMap =
                mapOf(
                    "user_id" to userId,
                    "email" to "test@example.com",
                    "name" to "Test User",
                    "nickname" to "testuser",
                )
            val uriSpec = mockk<RequestHeadersUriSpec<*>>()
            val getSpec = mockk<RestClient.RequestHeadersSpec<*>>()
            val retrieveSpec = mockk<RestClient.ResponseSpec>()
            every { restClient.get() } returns uriSpec
            every { uriSpec.uri(any<Function<UriBuilder, java.net.URI>>()) } returns getSpec
            every { getSpec.header(any(), any()) } returns getSpec
            every { getSpec.retrieve() } returns retrieveSpec
            every { retrieveSpec.body(any<ParameterizedTypeReference<Map<String, Any>>>()) } returns responseMap

            val result = auth0UsersService.getUserById("token", userId)
            assertNotNull(result)
            assertEquals(userId, result.user_id)
            assertEquals("test@example.com", result.email)
        }

        @Test
        fun `should return null if Auth0 returns null`() {
            val userId = UUID.randomUUID().toString()
            val uriSpec = mockk<RequestHeadersUriSpec<*>>()
            val getSpec = mockk<RestClient.RequestHeadersSpec<*>>()
            val retrieveSpec = mockk<RestClient.ResponseSpec>()
            every { restClient.get() } returns uriSpec
            every { uriSpec.uri(any<Function<UriBuilder, java.net.URI>>()) } returns getSpec
            every { getSpec.header(any(), any()) } returns getSpec
            every { getSpec.retrieve() } returns retrieveSpec
            every { retrieveSpec.body(any<ParameterizedTypeReference<Map<String, Any>>>()) } returns null

            val result = auth0UsersService.getUserById("token", userId)
            assertNull(result)
        }

        @Test
        fun `should return null if exception thrown`() {
            val userId = UUID.randomUUID().toString()
            val uriSpec = mockk<RequestHeadersUriSpec<*>>()
            val getSpec = mockk<RestClient.RequestHeadersSpec<*>>()
            val retrieveSpec = mockk<RestClient.ResponseSpec>()
            every { restClient.get() } returns uriSpec
            every { uriSpec.uri(any<Function<UriBuilder, java.net.URI>>()) } returns getSpec
            every { getSpec.header(any(), any()) } returns getSpec
            every { getSpec.retrieve() } returns retrieveSpec
            every { retrieveSpec.body(any<ParameterizedTypeReference<Map<String, Any>>>()) } throws RuntimeException("fail")

            val result = auth0UsersService.getUserById("token", userId)
            assertNull(result)
        }
    }
}
