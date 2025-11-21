package org.gudelker.snippet.service.modules.testsnippet

import org.gudelker.snippet.service.api.ResultType
import org.gudelker.snippet.service.modules.testsnippet.dto.TestSnippetResponseDto
import org.gudelker.snippet.service.modules.testsnippet.input.CreateTestSnippetRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
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
    @PostMapping
    fun createTestSnippet(
        @RequestBody request: CreateTestSnippetRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<TestSnippet> {
        val created = testSnippetService.createTestSnippet(request, jwt.subject)
        return ResponseEntity.ok(created)
    }

    @DeleteMapping("/{id}")
    fun deleteTestSnippet(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<String> {
        val id = UUID.fromString(id)
        testSnippetService.deleteTestSnippet(id)
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
}
