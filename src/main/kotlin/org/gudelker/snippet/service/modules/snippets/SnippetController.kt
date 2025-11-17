package org.gudelker.snippet.service.modules.snippets

import jakarta.validation.Valid
import org.gudelker.snippet.service.api.AssetApiClient
import org.gudelker.snippet.service.auth.CachedTokenService
import org.gudelker.snippet.service.modules.snippets.dto.PermissionType
import org.gudelker.snippet.service.modules.snippets.dto.create.SnippetFromFileResponse
import org.gudelker.snippet.service.modules.snippets.dto.share.ShareSnippetResponseDto
import org.gudelker.snippet.service.modules.snippets.dto.types.AccessType
import org.gudelker.snippet.service.modules.snippets.dto.types.DirectionType
import org.gudelker.snippet.service.modules.snippets.dto.types.SortByType
import org.gudelker.snippet.service.modules.snippets.dto.update.UpdateSnippetFromEditorResponse
import org.gudelker.snippet.service.modules.snippets.dto.update.UpdateSnippetFromFileResponse
import org.gudelker.snippet.service.modules.snippets.input.create.CreateSnippetFromEditor
import org.gudelker.snippet.service.modules.snippets.input.create.CreateSnippetFromFileInput
import org.gudelker.snippet.service.modules.snippets.input.share.ShareSnippetInput
import org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromEditorInput
import org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromFileInput
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient
import org.springframework.web.client.toEntity
import java.util.UUID

@RestController
@RequestMapping("/snippets")
class SnippetController(
    private val snippetService: SnippetService,
    private val cachedTokenService: CachedTokenService,
    private val restClient: RestClient,
    private val assetApiClient: AssetApiClient,
) {
    @GetMapping("/all")
    fun getAllSnippets(): List<Snippet> {
        return snippetService.getAllSnippets()
    }

    @PostMapping("")
    fun createSnippet(
        @RequestBody input: CreateSnippetFromEditor,
        @AuthenticationPrincipal jwt: Jwt,
    ): Snippet {
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

    @PutMapping("")
    fun updateSnippetFromEditor(
        @RequestBody @Valid input: UpdateSnippetFromEditorInput,
        @AuthenticationPrincipal jwt: Jwt,
    ): UpdateSnippetFromEditorResponse {
        return snippetService.updateSnippetFromEditor(
            input = input,
            jwt = jwt,
        )
    }

    @GetMapping("/user/{userId}")
    fun getSnippetsByUserId(
        @PathVariable userId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): List<Snippet> {
        return snippetService.getSnippetsByUserId(userId)
    }

    @GetMapping("/paginated")
    fun getSnippetsByFilter(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "ALL") accessType: AccessType,
        @RequestParam(defaultValue = "") name: String,
        @RequestParam(defaultValue = "") language: String,
        @RequestParam(defaultValue = "true") passedLint: Boolean,
        @RequestParam(defaultValue = "NAME") sortBy: SortByType,
        @RequestParam(defaultValue = "DESC") direction: DirectionType,
    ): Page<Snippet> {
        return snippetService.getSnippetsByFilter(jwt, page, pageSize, accessType, name, language, passedLint, sortBy, direction)
    }

    @GetMapping("/{snippetId}")
    fun getSnippetById(
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Snippet> {
        val userId = jwt.subject
        val token = cachedTokenService.getToken()
        println(token)
        println("**********************************************************************************")
        try {
            val permission: PermissionType? =
                restClient.get()
                    .uri(
                        "http://authorization:8080/api/permissions/{snippetId}?userId={userId}",
                        snippetId,
                        userId,
                    )
                    .header("Authorization", "Bearer $token")
                    .retrieve()
                    .toEntity<PermissionType>()
                    .body

            if (permission == null) {
                return ResponseEntity.status(403).build()
            }

            val snippet = snippetService.getSnippetById(snippetId)

            return ResponseEntity.ok(snippet)
        } catch (e: Exception) {
            println("Error calling authorization service: ${e.message}")
            e.printStackTrace()
            return ResponseEntity.status(500).build()
        }
    }

    @PatchMapping("/share/{snippetId}")
    fun shareSnippet(
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody input: ShareSnippetInput,
    ): ResponseEntity<ShareSnippetResponseDto> {
        return try {
            val response =
                snippetService.shareSnippet(
                    userId = jwt.subject,
                    sharedUserId = input.sharedUserId,
                    snippetId = UUID.fromString(snippetId),
                )
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(403).build()
        } catch (e: Exception) {
            println("Error sharing snippet: ${e.message}")
            e.printStackTrace()
            ResponseEntity.status(500).build()
        }
    }

    @GetMapping
    fun getSnippetsContentById(
        snippetId: String,
    ): String {
        val content = assetApiClient.getAsset(
            container = "snippets",
            key = snippetId,
        )
        return content
    }

}
