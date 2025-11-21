package org.gudelker.snippet.service.modules.snippets

import jakarta.validation.Valid
import org.gudelker.snippet.service.api.AssetApiClient
import org.gudelker.snippet.service.auth.CachedTokenService
import org.gudelker.snippet.service.modules.formatting.FormattingOrchestratorService
import org.gudelker.snippet.service.modules.snippets.dto.PermissionType
import org.gudelker.snippet.service.modules.snippets.dto.get.SnippetContentDto
import org.gudelker.snippet.service.modules.snippets.dto.get.SnippetWithComplianceDto
import org.gudelker.snippet.service.modules.snippets.dto.share.ShareSnippetResponseDto
import org.gudelker.snippet.service.modules.snippets.dto.types.AccessType
import org.gudelker.snippet.service.modules.snippets.dto.types.DirectionType
import org.gudelker.snippet.service.modules.snippets.dto.types.SortByType
import org.gudelker.snippet.service.modules.snippets.dto.update.UpdateSnippetFromEditorResponse
import org.gudelker.snippet.service.modules.snippets.input.create.CreateSnippetFromEditor
import org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromEditorInput
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
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
    private val formattingOrchestratorService: FormattingOrchestratorService,
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

    @PutMapping("/{snippetId}")
    fun updateSnippetFromEditor(
        @RequestBody @Valid input: UpdateSnippetFromEditorInput,
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): UpdateSnippetFromEditorResponse {
        return snippetService.updateSnippetFromEditor(
            input = input,
            jwt = jwt,
            snippetId,
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
        @RequestParam(required = false) passedLint: Boolean?,
        @RequestParam(defaultValue = "NAME") sortBy: SortByType,
        @RequestParam(defaultValue = "DESC") direction: DirectionType,
    ): Page<SnippetWithComplianceDto> {
        return snippetService.getSnippetsByFilter(jwt, page, pageSize, accessType, name, language, passedLint, sortBy, direction)
    }

    @GetMapping("/{snippetId}")
    fun getSnippetById(
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<SnippetContentDto> {
        val userId = jwt.subject
        val token = cachedTokenService.getToken()
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
            val content =
                assetApiClient.getAsset(
                    container = "snippets",
                    key = snippetId,
                )

            return ResponseEntity.ok(SnippetContentDto(content, snippet))
        } catch (e: Exception) {
            println("Error calling authorization service: ${e.message}")
            e.printStackTrace()
            return ResponseEntity.status(500).build()
        }
    }

    @PatchMapping("/share/{snippetId}/{sharedUserId}")
    fun shareSnippet(
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable sharedUserId: String,
    ): ResponseEntity<ShareSnippetResponseDto> {
        return try {
            val response =
                snippetService.shareSnippet(
                    userId = jwt.subject,
                    sharedUserId = sharedUserId,
                    snippetId = UUID.fromString(snippetId),
                )
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(403).build()
        } catch (e: Exception) {
            ResponseEntity.status(500).build()
        }
    }

    @DeleteMapping("/{snippetId}")
    fun deleteSnippet(
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Void> {
        return try {
            snippetService.deleteSnippet(snippetId, jwt.subject)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(403).build()
        } catch (e: Exception) {
            ResponseEntity.status(500).build()
        }
    }
}
