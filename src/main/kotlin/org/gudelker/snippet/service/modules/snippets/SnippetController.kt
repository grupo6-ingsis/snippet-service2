package org.gudelker.snippet.service.modules.snippets

import jakarta.validation.Valid
import org.gudelker.snippet.service.auth.CachedTokenService
import org.gudelker.snippet.service.modules.snippets.dto.PermissionType
import org.gudelker.snippet.service.modules.snippets.dto.create.SnippetFromFileResponse
import org.gudelker.snippet.service.modules.snippets.dto.update.UpdateSnippetFromEditorResponse
import org.gudelker.snippet.service.modules.snippets.dto.update.UpdateSnippetFromFileResponse
import org.gudelker.snippet.service.modules.snippets.input.create.CreateSnippetFromEditor
import org.gudelker.snippet.service.modules.snippets.input.create.CreateSnippetFromFileInput
import org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromEditorInput
import org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromFileInput
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient
import org.springframework.web.client.toEntity

@RestController
@RequestMapping("/snippets")
class SnippetController(
    private val snippetService: SnippetService,
    private val cachedTokenService: CachedTokenService,
    private val restClient: RestClient,
) {
    @GetMapping("/all")
    fun getAllSnippets(
    ): List<Snippet> {
        return snippetService.getAllSnippets()
    }

    @PostMapping("/create")
    fun createSnippet(@RequestBody input: CreateSnippetFromEditor, @AuthenticationPrincipal jwt: Jwt): Snippet {
        return snippetService.createSnippetFromEditor(input, jwt)
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
    ): UpdateSnippetFromFileResponse {
        return snippetService.updateSnippetFromFile(
            input = input,
        )
    }

    @PutMapping("/update")
    fun updateSnippetFromEditor(
        @RequestBody @Valid input: UpdateSnippetFromEditorInput,
        @AuthenticationPrincipal jwt: Jwt,
    ): UpdateSnippetFromEditorResponse {
        return snippetService.updateSnippetFromEditor(
            input = input,
            jwt = jwt,
        )
    }

    @GetMapping("/{userId}")
    fun getSnippetsByUserId(
        @PathVariable userId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): List<Snippet> {
        return snippetService.getSnippetsByUserId(userId)
    }

    @GetMapping("/get/filter")
    fun getSnippetsByFilter(
        @RequestParam(defaultValue = "all" ) accessType: String, // sería si es autor, compartido o all
        @RequestParam(defaultValue = "") name: String,
        @RequestParam(defaultValue = "") language: String,
        @RequestParam(defaultValue = "true") passedLint: Boolean,
        @RequestParam(defaultValue = "name") sortBy: String, // ordena por name, language, passedLint
        @RequestParam(defaultValue = "desc") direction: String // asc o desc
     ): List<Snippet> {
        return snippetService.getSnippetsByFilter(accessType, name, language, passedLint, sortBy, direction)
    }

    @GetMapping("/{snippetId}")
    fun getSnippetById(
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Snippet> {
        val userId = jwt.subject
        val token = cachedTokenService.getToken()

        val permissions: List<PermissionType> =
            restClient.get()
                .uri { builder ->
                    builder.path("http://authorization:8080/permissions/{snippetId}")
                        .queryParam("userId", userId)
                        .build(snippetId)
                }
                .header("Authorization", "Bearer $token")
                .retrieve()
                .toEntity<List<PermissionType>>()
                .body ?: emptyList()

        if (PermissionType.READ !in permissions) {
            return ResponseEntity.status(403).build()
        }

        val snippet = snippetService.getSnippetById(snippetId)

        return ResponseEntity.ok(snippet)
    }
}