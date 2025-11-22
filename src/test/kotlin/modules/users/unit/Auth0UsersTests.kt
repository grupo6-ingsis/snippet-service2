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
import java.net.URI
import java.util.UUID
import java.util.function.Function
import kotlin.apply
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.text.get

class Auth0UsersTests {
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

        @Test
        fun `should return users with all fields present and query not blank`() {
            val user =
                Auth0User(
                    user_id = "id123",
                    email = "user@email.com",
                    name = "User Name",
                    nickname = "nick",
                    picture = "http://pic.url",
                    created_at = "2023-01-01T00:00:00Z",
                    updated_at = "2023-01-02T00:00:00Z",
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
                                "picture" to user.picture,
                                "created_at" to user.created_at,
                                "updated_at" to user.updated_at,
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

            val result = auth0UsersService.searchUsers("token", "notblank", 0, 10)
            assertEquals(1, result.users.size)
            val u = result.users[0]
            assertEquals(user.user_id, u.user_id)
            assertEquals(user.email, u.email)
            assertEquals(user.name, u.name)
            assertEquals(user.nickname, u.nickname)
            assertEquals(user.picture, u.picture)
            assertEquals(user.created_at, u.created_at)
            assertEquals(user.updated_at, u.updated_at)
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

        @Test
        fun `should return user with all fields present`() {
            val userId = "id123"
            val responseMap =
                mapOf(
                    "user_id" to userId,
                    "email" to "user@email.com",
                    "name" to "User Name",
                    "nickname" to "nick",
                    "picture" to "http://pic.url",
                    "created_at" to "2023-01-01T00:00:00Z",
                    "updated_at" to "2023-01-02T00:00:00Z",
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
            assertEquals("user@email.com", result.email)
            assertEquals("User Name", result.name)
            assertEquals("nick", result.nickname)
            assertEquals("http://pic.url", result.picture)
            assertEquals("2023-01-01T00:00:00Z", result.created_at)
            assertEquals("2023-01-02T00:00:00Z", result.updated_at)
        }
    }

    @Nested
    inner class SearchUsersEdgeCases {
        @Test
        fun `should handle total, start, limit as String`() {
            val responseMap: Map<String, Any> =
                mapOf(
                    "users" to listOf(mapOf("user_id" to "id")),
                    "total" to "1",
                    "start" to "0",
                    "limit" to "5",
                )
            val uriSpec = mockk<RequestHeadersUriSpec<*>>()
            val getSpec = mockk<RestClient.RequestHeadersSpec<*>>()
            val retrieveSpec = mockk<RestClient.ResponseSpec>()
            every { restClient.get() } returns uriSpec
            every { uriSpec.uri(any<Function<UriBuilder, java.net.URI>>()) } returns getSpec
            every { getSpec.header(any(), any()) } returns getSpec
            every { getSpec.retrieve() } returns retrieveSpec
            every { retrieveSpec.body(any<ParameterizedTypeReference<Map<String, Any>>>()) } returns responseMap
            val result = auth0UsersService.searchUsers("token", "", 0, 5)
            assertEquals(1, result.total)
            assertEquals(0, result.start)
            assertEquals(5, result.limit)
            assertEquals("id", result.users[0].user_id)
        }

        @Test
        fun `should handle total, start, limit as unexpected type`() {
            val responseMap: Map<String, Any> =
                mapOf(
                    "users" to emptyList<Any>(),
                    "total" to listOf(1),
                    "start" to mapOf("a" to 1),
                    "limit" to 7,
                )
            val uriSpec = mockk<RequestHeadersUriSpec<*>>()
            val getSpec = mockk<RestClient.RequestHeadersSpec<*>>()
            val retrieveSpec = mockk<RestClient.ResponseSpec>()
            every { restClient.get() } returns uriSpec
            every { uriSpec.uri(any<Function<UriBuilder, java.net.URI>>()) } returns getSpec
            every { getSpec.header(any(), any()) } returns getSpec
            every { getSpec.retrieve() } returns retrieveSpec
            every { retrieveSpec.body(any<ParameterizedTypeReference<Map<String, Any>>>()) } returns responseMap
            val result = auth0UsersService.searchUsers("token", "", 0, 7)
            assertEquals(0, result.total)
            assertEquals(0, result.start)
            assertEquals(7, result.limit)
        }

        @Test
        fun `should handle users as not a list`() {
            val responseMap: Map<String, Any> =
                mapOf(
                    "users" to 123,
                    "total" to 0,
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
            val result = auth0UsersService.searchUsers("token", "", 0, 10)
            assertTrue(result.users.isEmpty())
        }

        @Test
        fun `should handle user object not a map`() {
            val responseMap: Map<String, Any> =
                mapOf(
                    "users" to listOf(123),
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
            val result = auth0UsersService.searchUsers("token", "", 0, 10)
            assertTrue(result.users.isEmpty())
        }

        @Test
        fun `should handle user object not a map and null user_id`() {
            val responseMap: Map<String, Any> =
                mapOf(
                    "users" to listOf(null),
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
            val result = auth0UsersService.searchUsers("token", "", 0, 10)
            assertTrue(result.users.isEmpty())
        }

        @Test
        fun `should handle exception parsing user`() {
            val responseMap: Map<String, Any> =
                mapOf(
                    "users" to
                        listOf(
                            object : Map<String, Any> by emptyMap<String, Any>() {
                                override fun get(key: String): Any? = throw RuntimeException("fail parse")
                            },
                        ),
                    "total" to 1,
                    "start" to 0,
                    "limit" to 10,
                )
            val uriSpec = mockk<RequestHeadersUriSpec<*>>()
            val getSpec = mockk<RestClient.RequestHeadersSpec<*>>()
            val retrieveSpec = mockk<RestClient.ResponseSpec>()
            every { restClient.get() } returns uriSpec
            every { uriSpec.uri(any<java.net.URI>()) } returns getSpec
            every { getSpec.header(any(), any()) } returns getSpec
            every { getSpec.retrieve() } returns retrieveSpec
            every { retrieveSpec.body(any<ParameterizedTypeReference<Map<String, Any>>>()) } returns responseMap
            val result = auth0UsersService.searchUsers("token", "", 0, 10)
            assertTrue(result.users.isEmpty())
        }
    }

    @Nested
    inner class UriBuilderLogic {
        @Test
        fun `should build uri with correct params for searchUsers`() {
            val uriSpec = mockk<RequestHeadersUriSpec<*>>()
            val getSpec = mockk<RestClient.RequestHeadersSpec<*>>()
            val retrieveSpec = mockk<RestClient.ResponseSpec>()
            val builtUri = java.net.URI("https://test.auth0.com/api/v2/users")
            val uriBuilder = mockk<UriBuilder>(relaxed = true)
            every { uriBuilder.scheme(any()) } returns uriBuilder
            every { uriBuilder.host(any()) } returns uriBuilder
            every { uriBuilder.path(any()) } returns uriBuilder
            every { uriBuilder.queryParam(any(), any()) } returns uriBuilder
            every { uriBuilder.build() } returns builtUri
            every { restClient.get() } returns uriSpec
            every { uriSpec.uri(any<Function<UriBuilder, java.net.URI>>()) } answers {
                val fn = firstArg<Function<UriBuilder, java.net.URI>>()
                fn.apply(uriBuilder)
                getSpec
            }
            every { getSpec.header(any(), any()) } returns getSpec
            every { getSpec.retrieve() } returns retrieveSpec
            every {
                retrieveSpec.body(any<ParameterizedTypeReference<Map<String, Any>>>())
            } returns mapOf("users" to emptyList<Any>(), "total" to 0, "start" to 0, "limit" to 10)
            auth0UsersService.searchUsers("token", "test", 1, 10)
        }
    }
}
