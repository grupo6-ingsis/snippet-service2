package org.gudelker.snippet.service.modules.testsnippet

import org.gudelker.snippet.service.modules.snippets.SnippetRepository
import org.gudelker.snippet.service.modules.testsnippet.input.CreateTestSnippetRequest
import org.springframework.stereotype.Service
import java.util.UUID

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
                input = request.input
                expectedOutput = request.expectedOutput
                this.snippet = snippet
            }
        return testSnippetRepository.save(testSnippet)
    }

    fun deleteTestSnippet(id: UUID) {
        testSnippetRepository.deleteById(id)
    }
}
