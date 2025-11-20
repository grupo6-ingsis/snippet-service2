package org.gudelker.snippet.service.modules.testsnippet

import org.gudelker.snippet.service.api.AssetApiClient
import org.gudelker.snippet.service.api.AuthApiClient
import org.gudelker.snippet.service.api.EngineApiClient
import org.gudelker.snippet.service.modules.interpret.InterpretSnippetRequest
import org.gudelker.snippet.service.modules.interpret.InterpretSnippetResponse
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
    private val engineApiClient: EngineApiClient,
    private val assetApiClient: AssetApiClient,
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

    fun runTestSnippets(
        testCase: CreateTestSnippetRequest,
        userId: String,
    ): InterpretSnippetResponse {
        println("Running test snippet for snippetId: ${testCase.snippetId} by userId: $userId")
        val permission = authApiClient.hasPermission(testCase.snippetId, userId)
        println("Permission: $permission")
        if (permission == null) {
            throw IllegalAccessException("User is not authorized to run test snippets for this snippet")
        }
        val testSnippet =
            testSnippetRepository.findById(UUID.fromString(testCase.id))
                .orElseThrow { IllegalArgumentException("TestSnippet not found") }
        val snippet =
            snippetRepository.findById(UUID.fromString(testCase.snippetId))
                .orElseThrow { IllegalArgumentException("Snippet not found") }
        val content = assetApiClient.getAsset("snippets", testCase.snippetId)
        val interpretRequest =
            InterpretSnippetRequest(
                content,
                snippet.languageVersion.version,
                testSnippet.input ?: mutableListOf(),
            )
        val result = engineApiClient.interpretSnippet(interpretRequest)
        println("Interpretation result: $result")
        return result
    }
}
