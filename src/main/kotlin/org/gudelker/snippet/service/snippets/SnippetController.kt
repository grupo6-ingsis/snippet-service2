package org.gudelker.snippet.service.snippets

import jakarta.validation.Valid
import org.gudelker.snippet.service.snippets.dto.SnippetDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("snippets")
class SnippetController (
    private val snippetService: SnippetService
) {
    @GetMapping("")
    fun getAllSnippets(): List<Snippet> {
        return snippetService.getAllSnippets()
    }

    @PostMapping("")
    fun createSnippet(@RequestBody @Valid snippetDto: SnippetDto): Snippet {
        return snippetService.createSnippet(
            userId = snippetDto.userId,
            title = snippetDto.title,
            content = snippetDto.content
        )
    }

    @GetMapping("/{userId}")
    fun getSnippetsByUserId(@PathVariable userId: String): List<Snippet> {
        return snippetService.getSnippetsByUserId(userId)
    }
}