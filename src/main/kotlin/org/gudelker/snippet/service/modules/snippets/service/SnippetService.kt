package org.gudelker.snippet.service.modules.snippets.service
import org.gudelker.snippet.service.modules.snippets.Snippet
import org.gudelker.snippet.service.modules.snippets.dto.authorization.AuthorizeRequestDto
import org.gudelker.snippet.service.modules.snippets.dto.authorization.AuthorizeResponseDto
import org.gudelker.snippet.service.modules.snippets.dto.create.SnippetFromFileResponse
import org.gudelker.snippet.service.modules.snippets.dto.update.UpdateSnippetFromFileResponse
import org.gudelker.snippet.service.modules.snippets.input.create.CreateSnippetFromFileInput
import org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromFileInput
import org.gudelker.snippet.service.modules.snippets.repository.SnippetRepository
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.OffsetDateTime
import java.util.UUID

@Service
class SnippetService(private val snippetRepository: SnippetRepository, private val restClient: RestClient) {
    fun getAllSnippets(): List<Snippet> {
        return snippetRepository.findAll()
    }

    fun createSnippetFromFile(
        input: CreateSnippetFromFileInput,
        jwt: Jwt,
    ): SnippetFromFileResponse {
        val snippetId = UUID.randomUUID()
        val request = createAuthorizeRequestDto(jwt.id, listOf("OWNER"))
        val authorization = authorizeSnippet(snippetId, request, jwt.tokenValue)
        if (!authorization.success) {
            throw RuntimeException("Authorization failed: ${authorization.message}")
        }
        val snippet = createSnippet(snippetId, jwt.id, input)
        snippetRepository.save(snippet)
        return createSnippetFromFileResponse(input, jwt.id)
    }

    fun getSnippetsByUserId(userId: String): List<Snippet> {
        return snippetRepository.findByOwnerId(userId)
    }

    fun updateSnippetFromFile(
        input: UpdateSnippetFromFileInput,
        jwt: Jwt,
    ): UpdateSnippetFromFileResponse {
        if (input.title == null && input.content == null && input.language == null) {
            throw IllegalArgumentException("At least one attribute (title, content, language) must be provided for update.")
        }
        val snippetId = UUID.fromString(input.snippetId)
        val snippet =
            snippetRepository.findById(snippetId)
                .orElseThrow { RuntimeException("Snippet not found") }

        val authorization = authorizeUpdateSnippet(input.snippetId, jwt.tokenValue)
        if (!authorization) {
            throw RuntimeException("Authorization failed")
        }

        input.title?.let { snippet.title = it }
        input.content?.let { snippet.content = it }
        input.language?.let { snippet.language = it }
        snippet.updated = OffsetDateTime.now()

        snippetRepository.save(snippet)
        return UpdateSnippetFromFileResponse(
            snippetId = snippet.id.toString(),
            title = snippet.title,
            content = snippet.content,
            language = snippet.language,
            updated = snippet.updated,
        )
    }

    private fun authorizeUpdateSnippet(
        snippetId: String,
        jwtToken: String,
    ): Boolean {
        val url = "http://authorization-service/authorize-update/$snippetId"
        val response =
            restClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")
                .retrieve()
                .body(Boolean::class.java)
        return response ?: throw RuntimeException("No response from authorization service")
    }

    private fun authorizeSnippet(
        snippetId: UUID,
        request: AuthorizeRequestDto,
        jwtToken: String,
    ): AuthorizeResponseDto {
        val url = "http://authorization-service/authorize/$snippetId"
        val response =
            restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")
                .body(request)
                .retrieve()
                .body(AuthorizeResponseDto::class.java)
        return response ?: throw RuntimeException("No response from authorization service")
    }

    private fun createAuthorizeRequestDto(
        userId: String,
        permissions: List<String>,
    ): AuthorizeRequestDto {
        return AuthorizeRequestDto(
            userId = userId,
            permissions = permissions,
        )
    }

    private fun createSnippet(
        id: UUID,
        ownerId: String,
        input: CreateSnippetFromFileInput,
    ): Snippet {
        return Snippet(
            id = id,
            ownerId = ownerId,
            title = input.title,
            content = input.content,
            language = input.language,
            created = OffsetDateTime.now(),
            updated = OffsetDateTime.now(),
        )
    }

    private fun createSnippetFromFileResponse(
        input: CreateSnippetFromFileInput,
        userId: String,
    ): SnippetFromFileResponse {
        return SnippetFromFileResponse(input.title, input.content, userId)
    }
}
