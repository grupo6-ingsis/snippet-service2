package org.gudelker.snippet.service.modules.testsnippet

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
) {
    fun createTestSnippet(request: CreateTestSnippetRequest): TestSnippet {
        val snippet =
            snippetRepository.findById(UUID.fromString(request.snippetId))
                .orElseThrow { IllegalArgumentException("Snippet not found") }
        val testSnippet =
            TestSnippet().apply {
                id = UUID.fromString(request.id)
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
        return testSnippetRepository.findAllBySnippetId(snippetId).map { testSnippet ->
            TestSnippetResponseDto(
                name = testSnippet.snippet.toString(),
                input = testSnippet.input,
                output = testSnippet.expectedOutput,
                snippetId = testSnippet.snippet.id?.toString(),
            )
        }
    }
}
