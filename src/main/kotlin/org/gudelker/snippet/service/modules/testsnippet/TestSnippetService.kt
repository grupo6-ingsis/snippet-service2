package org.gudelker.snippet.service.modules.testsnippet

import org.gudelker.snippet.service.api.AuthApiClient
import org.gudelker.snippet.service.modules.snippets.SnippetRepository
import org.gudelker.snippet.service.modules.testsnippet.dto.TestSnippetResponseDto
import org.gudelker.snippet.service.modules.testsnippet.input.CreateTestSnippetRequest
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.toString

@Service
class TestSnippetService(
    private val testSnippetRepository: TestSnippetRepository,
    private val snippetRepository: SnippetRepository,
    private val authApiClient: AuthApiClient,
) {
    fun createTestSnippet(
        request: CreateTestSnippetRequest,
        userId: String,
    ): TestSnippet {
        val snippet =
            snippetRepository.findById(UUID.fromString(request.snippetId))
                .orElseThrow { IllegalArgumentException("Snippet not found") }
        val isAuthorized =
            authApiClient.isUserAuthorizedToWriteSnippet(
                snippetId = request.snippetId,
                userId = userId,
            )
        if (!isAuthorized) {
            throw IllegalAccessException("User is not authorized to add test snippets to this snippet")
        }
        val testSnippet =
            TestSnippet().apply {
                name = request.name
                input = request.input
                expectedOutput = request.expectedOutput
                this.snippet = snippet
            }
        val saved = testSnippetRepository.save(testSnippet)
        return saved
    }

    fun deleteTestSnippet(id: UUID) {
        testSnippetRepository.deleteById(id)
    }

    fun getTestSnippetsBySnippetId(snippetId: UUID): List<TestSnippetResponseDto> {
        snippetRepository.findById(snippetId)
            .orElseThrow { IllegalArgumentException("Snippet not found") }
        return testSnippetRepository.findAllBySnippetId(snippetId).map { testSnippet ->
            TestSnippetResponseDto(
                name = testSnippet.name,
                input = testSnippet.input,
                output = testSnippet.expectedOutput,
                snippetId = testSnippet.snippet.id.toString(),
                id = testSnippet.id.toString(),
            )
        }
    }
}
