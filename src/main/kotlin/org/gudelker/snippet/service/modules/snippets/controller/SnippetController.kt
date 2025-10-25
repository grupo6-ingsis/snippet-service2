package org.gudelker.snippet.service.modules.snippets.controller

import jakarta.validation.Valid
import org.gudelker.snippet.service.modules.snippets.Snippet
import org.gudelker.snippet.service.modules.snippets.dto.create.SnippetFromFileResponse
import org.gudelker.snippet.service.modules.snippets.dto.update.UpdateSnippetFromFileResponse
import org.gudelker.snippet.service.modules.snippets.input.create.CreateSnippetFromFileInput
import org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromFileInput
import org.gudelker.snippet.service.modules.snippets.service.SnippetService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/snippets")
class SnippetController(
    private val snippetService: SnippetService,
) {
    @GetMapping("/all")
    fun getAllSnippets(
        @AuthenticationPrincipal jwt: Jwt,
    ): List<Snippet> {
        return snippetService.getAllSnippets()
    }

    @PostMapping("/file")
    fun createSnippetFromFile(
        @RequestBody @Valid input: CreateSnippetFromFileInput,
        @AuthenticationPrincipal jwt: Jwt,
    ): SnippetFromFileResponse {
        return snippetService.createSnippetFromFile(
            jwt = jwt,
            input = input,
        )
    }

    @PutMapping("/file")
    fun updateSnippetFromFile(
        @RequestBody @Valid input: UpdateSnippetFromFileInput,
        @AuthenticationPrincipal jwt: Jwt,
    ): UpdateSnippetFromFileResponse {
        return snippetService.updateSnippetFromFile(
            jwt = jwt,
            input = input,
        )
    }

    @GetMapping("/{userId}")
    fun getSnippetsByUserId(
        @PathVariable userId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): List<Snippet> {
        return snippetService.getSnippetsByUserId(userId)
    }
}
