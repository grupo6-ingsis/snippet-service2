package org.gudelker.snippet.service.modules.snippets.service
import org.gudelker.snippet.service.modules.snippets.Snippet
import org.gudelker.snippet.service.modules.snippets.dto.AuthorizeRequestDto
import org.gudelker.snippet.service.modules.snippets.dto.AuthorizeResponseDto
import org.gudelker.snippet.service.modules.snippets.dto.SnippetFromFileResponse
import org.gudelker.snippet.service.modules.snippets.input.CreateSnippetFromFileInput
import org.gudelker.snippet.service.modules.snippets.repository.SnippetRepository
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.client.RestClient
import java.util.UUID

@Service
class SnippetService (private val snippetRepository: SnippetRepository,private val restClient: RestClient) {

    fun getAllSnippets(): List<Snippet> {
        return snippetRepository.findAll()
    }

    fun createSnippetFromFile(input: CreateSnippetFromFileInput, jwt: Jwt): SnippetFromFileResponse {
        val snippetId = UUID.randomUUID()
        val request = AuthorizeRequestDto(
            userId = jwt.id,
            permissions = listOf("OWNER")
        )
        val authorization = authorizeSnippet(snippetId, request, jwt.tokenValue)
        if (!authorization.success) {
            throw RuntimeException("Authorization failed: ${authorization.message}")
        }
        val snippet = createSnippet(snippetId, jwt.id, input)
        snippetRepository.save(snippet)
        return createSnippetFromFileResponse(input, jwt.id)
    }
    private fun createSnippet(
        id: UUID,
        ownerId: String,
        input: CreateSnippetFromFileInput
    ): Snippet {
        return Snippet(
            id = id,
            ownerId = ownerId,
            title = input.title,
            content = input.description,
            language = input.language,
            created = OffsetDateTime.now(),
            updated = OffsetDateTime.now()
        )
    }


    private fun createSnippetFromFileResponse(input: CreateSnippetFromFileInput, userId: String): SnippetFromFileResponse{
        return SnippetFromFileResponse(input.title,input.description,userId)
    }

    fun getSnippetsByUserId(userId: String): List<Snippet> {
        return snippetRepository.findByUserId(userId)
    }

    private fun authorizeSnippet(snippetId: UUID, request: AuthorizeRequestDto, jwtToken: String): AuthorizeResponseDto {
        val url = "http://authorization-service/authorize/$snippetId"
        val response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtToken")
            .body(request)
            .retrieve()
            .body(AuthorizeResponseDto::class.java)
        return response ?: throw RuntimeException("No response from authorization service")
    }




}