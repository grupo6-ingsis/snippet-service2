package org.gudelker.snippet.service.modules.testsnippet

import org.gudelker.snippet.service.modules.testsnippet.dto.TestSnippetResponseDto
import org.gudelker.snippet.service.modules.testsnippet.input.CreateTestSnippetRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
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

    @PostMapping("/snippetId")
    fun getTestSnippetsBySnippetId(
        @RequestBody snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<List<TestSnippetResponseDto>> {
        val id: UUID = UUID.fromString(snippetId)
        println("id ESTE ES EL IDDD: $id")
        val testSnippets = testSnippetService.getTestSnippetsBySnippetId(id)
        return ResponseEntity.ok(testSnippets)
    }
}
