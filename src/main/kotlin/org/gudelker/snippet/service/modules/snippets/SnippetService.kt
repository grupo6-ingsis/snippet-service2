package org.gudelker.snippet.service.modules.snippets

import org.gudelker.snippet.service.api.AuthApiClient
import org.gudelker.snippet.service.modules.snippets.dto.authorization.AuthorizeRequestDto
import org.gudelker.snippet.service.modules.snippets.dto.create.SnippetFromFileResponse
import org.gudelker.snippet.service.modules.snippets.dto.update.UpdateSnippetFromFileResponse
import org.gudelker.snippet.service.modules.snippets.input.create.CreateSnippetFromFileInput
import org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromFileInput
import org.gudelker.snippet.service.modules.snippets.SnippetRepository
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class SnippetService(private val snippetRepository: SnippetRepository, private val authApiClient: AuthApiClient) {
    fun getAllSnippets(): List<Snippet> {
        return snippetRepository.findAll()
    }

    fun createSnippetFromFile(
        input: CreateSnippetFromFileInput,
        jwt: Jwt,
    ): SnippetFromFileResponse {
        val snippetId = UUID.randomUUID()
        val userId =
            jwt.claims["sub"] as? String
                ?: throw IllegalArgumentException("JWT missing 'sub' claim")
        println("✅ User ID: $userId")
        val request = createAuthorizeRequestDto(userId, listOf("OWNER"))
        println("✅ Authorization request: $request")
        try {
            val authorization = authApiClient.authorizeSnippet(snippetId, request)
            println("🟩 Auth service response: $authorization")
        } catch (ex: Exception) {
            println("🟥 Error calling Auth service: ${ex::class.simpleName} - ${ex.message}")
            ex.printStackTrace()
            throw ex
        }
        val snippet = createSnippet(snippetId, userId, input)
        println("✅ Snippet to save: $snippet")
        snippetRepository.save(snippet)
        return createSnippetFromFileResponse(input, userId)
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

        val authorization = authApiClient.authorizeUpdateSnippet(input.snippetId, jwt.tokenValue)
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

    fun getSnippetById(snippetId: String): Snippet {
        return snippetRepository.findById(UUID.fromString(snippetId))
            .orElseThrow { RuntimeException("Snippet not found") }
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