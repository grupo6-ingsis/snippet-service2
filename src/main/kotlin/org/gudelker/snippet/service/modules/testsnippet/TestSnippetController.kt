package org.gudelker.snippet.service.modules.testsnippet

import org.gudelker.snippet.service.api.ResultType
import org.gudelker.snippet.service.modules.testsnippet.dto.CreateTestSnippetResponseDto
import org.gudelker.snippet.service.modules.testsnippet.dto.TestSnippetResponseDto
import org.gudelker.snippet.service.modules.testsnippet.input.CreateTestSnippetRequest
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/testsnippet")
class TestSnippetController(
    private val testSnippetService: TestSnippetService,
) {
    private val logger = LoggerFactory.getLogger(TestSnippetController::class.java)

    @PostMapping
    fun createTestSnippet(
        @RequestBody request: CreateTestSnippetRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<CreateTestSnippetResponseDto> {
        val userId = jwt.subject
        logger.info("Creating test snippet. Request by user: {} - SnippetId: {}, TestName: {}", userId, request.snippetId, request.name)
        return try {
            val created = testSnippetService.createTestSnippet(request, userId)
            logger.info("Successfully created test snippet (ID: {}) for snippet: {}", created.id, created.snippet.id)
            ResponseEntity.ok(
                CreateTestSnippetResponseDto(
                    id = created.id.toString(),
                    snippetId = created.snippet.id.toString(),
                    name = created.name,
                    input = created.input,
                    expectedOutput = created.expectedOutput,
                ),
            )
        } catch (e: Exception) {
            logger.error("Failed to create test snippet for snippet: {} - User: {} - Error: {}", request.snippetId, userId, e.message, e)
            throw e
        }
    }

    @DeleteMapping("/{id}")
    fun deleteTestSnippet(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<String> {
        val uuid = UUID.fromString(id)
        testSnippetService.deleteTestSnippet(uuid)
        return "Deleted successfully".let { ResponseEntity.ok(it) }
    }

    @GetMapping
    fun getTestSnippetsBySnippetId(
        @RequestParam snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<List<TestSnippetResponseDto>> {
        val id = UUID.fromString(snippetId)
        println("id ESTE ES EL IDDD: $id")
        val testSnippets = testSnippetService.getTestSnippetsBySnippetId(id)
        return ResponseEntity.ok(testSnippets)
    }

    @PostMapping("/run")
    fun runTestSnippets(
        @RequestBody testCase: CreateTestSnippetRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<ResultType> {
        println("Running test snippet for snippetId: ${testCase.snippetId} by userId: ${jwt.subject}")
        val result = testSnippetService.runTestSnippets(testCase, jwt.subject)
        println(result)
        return ResponseEntity.ok(result)
    }

    @PutMapping("/update/{id}")
    fun updateTestSnippet(
        @PathVariable id: String,
        @RequestBody request: CreateTestSnippetRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<CreateTestSnippetResponseDto> {
        val updated = testSnippetService.updateTestSnippet(id, request, jwt.subject)
        return ResponseEntity.ok(
            CreateTestSnippetResponseDto(
                id = updated.id.toString(),
                snippetId = updated.snippet.id.toString(),
                name = updated.name,
                input = updated.input,
                expectedOutput = updated.expectedOutput,
            ),
        )
    }
}
