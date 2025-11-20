package org.gudelker.snippet.service.modules.testsnippet

import org.gudelker.snippet.service.modules.testsnippet.input.CreateTestSnippetRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/testsnippets")
class TestSnippetController(
    private val testSnippetService: TestSnippetService,
) {
    @PostMapping
    fun createTestSnippet(
        @RequestBody request: CreateTestSnippetRequest,
    ): ResponseEntity<TestSnippet> {
        val created = testSnippetService.createTestSnippet(request)
        return ResponseEntity.ok(created)
    }

    @DeleteMapping("/{id}")
    fun deleteTestSnippet(
        @PathVariable id: UUID,
    ): ResponseEntity<Void> {
        testSnippetService.deleteTestSnippet(id)
        return ResponseEntity.noContent().build()
    }
}
