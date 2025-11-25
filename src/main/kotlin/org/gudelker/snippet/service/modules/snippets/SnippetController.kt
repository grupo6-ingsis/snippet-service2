package org.gudelker.snippet.service.modules.snippets

import jakarta.validation.Valid
import org.gudelker.snippet.service.api.AssetApiClient
import org.gudelker.snippet.service.auth.CachedTokenService
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
import org.slf4j.LoggerFactory
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
) {
    private val logger = LoggerFactory.getLogger(SnippetController::class.java)

    @GetMapping("/all")
    fun getAllSnippets(): List<Snippet> {
        logger.info("Fetching all snippets")
        val snippets = snippetService.getAllSnippets()
        logger.info("Successfully retrieved {} snippets", snippets.size)
        return snippets
    }

    @PostMapping("")
    fun createSnippet(
        @RequestBody input: CreateSnippetFromEditor,
        @AuthenticationPrincipal jwt: Jwt,
    ): Snippet {
        logger.info("Creating snippet for user: {} with title: {}", jwt.subject, input.title)
        val snippet = snippetService.createSnippetFromEditor(input, jwt)
        logger.info("Successfully created snippet with id: {} for user: {}", snippet.id, jwt.subject)
        return snippet
    }

    @PutMapping("/{snippetId}")
    fun updateSnippetFromEditor(
        @RequestBody @Valid input: UpdateSnippetFromEditorInput,
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): UpdateSnippetFromEditorResponse {
        logger.info("Updating snippet: {} by user: {}", snippetId, jwt.subject)
        val response = snippetService.updateSnippetFromEditor(input = input, jwt = jwt, snippetId)
        logger.info("Successfully updated snippet: {} by user: {}", snippetId, jwt.subject)
        return response
    }

    @GetMapping("/user/{userId}")
    fun getSnippetsByUserId(
        @PathVariable userId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): List<Snippet> {
        logger.info("Fetching snippets for userId: {} requested by: {}", userId, jwt.subject)
        val snippets = snippetService.getSnippetsByUserId(userId)
        logger.info("Retrieved {} snippets for userId: {}", snippets.size, userId)
        return snippets
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
        logger.info(
            "Fetching paginated snippets for user: {} - page: {}, pageSize: {}, " +
                "accessType: {}, name: '{}', language: '{}', passedLint: {}, sortBy: {}, direction: {}",
            jwt.subject, page, pageSize, accessType, name, language, passedLint, sortBy, direction,
        )
        val result = snippetService.getSnippetsByFilter(jwt, page, pageSize, accessType, name, language, passedLint, sortBy, direction)
        logger.info(
            "Retrieved {} snippets (page {}/{}) for user: {}",
            result.numberOfElements,
            result.number + 1,
            result.totalPages,
            jwt.subject,
        )
        return result
    }

    @GetMapping("/{snippetId}")
    fun getSnippetById(
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<SnippetContentDto> {
        logger.info("User: {} requesting snippet: {}", jwt.subject, snippetId)
        val userId = jwt.subject
        val token = cachedTokenService.getToken()
        try {
            logger.debug("Checking permissions for user: {} on snippet: {}", userId, snippetId)
            val permission: PermissionType? =
                restClient.get()
                    .uri("http://authorization:8080/api/permissions/{snippetId}?userId={userId}", snippetId, userId)
                    .header("Authorization", "Bearer $token")
                    .retrieve()
                    .toEntity<PermissionType>()
                    .body

            if (permission == null) {
                logger.warn("Access denied: User {} does not have permission for snippet: {}", userId, snippetId)
                return ResponseEntity.status(403).build()
            }

            logger.debug("Permission granted: {} for user: {} on snippet: {}", permission, userId, snippetId)
            val snippet = snippetService.getSnippetById(snippetId)
            val content = assetApiClient.getAsset(container = "snippets", key = snippetId)

            logger.info("Successfully retrieved snippet: {} for user: {}", snippetId, userId)
            return ResponseEntity.ok(SnippetContentDto(content, snippet))
        } catch (e: Exception) {
            logger.error("Error retrieving snippet: {} for user: {} - Error: {}", snippetId, userId, e.message, e)
            return ResponseEntity.status(500).build()
        }
    }

    @PatchMapping("/share/{snippetId}/{sharedUserId}")
    fun shareSnippet(
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable sharedUserId: String,
    ): ResponseEntity<ShareSnippetResponseDto> {
        logger.info("User: {} attempting to share snippet: {} with user: {}", jwt.subject, snippetId, sharedUserId)
        return try {
            val response =
                snippetService.shareSnippet(
                    userId = jwt.subject,
                    sharedUserId = sharedUserId,
                    snippetId = UUID.fromString(snippetId),
                )
            logger.info("Successfully shared snippet: {} from user: {} to user: {}", snippetId, jwt.subject, sharedUserId)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid argument when sharing snippet: {} - Error: {}", snippetId, e.message)
            ResponseEntity.badRequest().build()
        } catch (e: AccessDeniedException) {
            logger.warn("Access denied: User {} cannot share snippet: {}", jwt.subject, snippetId)
            ResponseEntity.status(403).build()
        } catch (e: Exception) {
            logger.error(
                "Unexpected error sharing snippet: {} from user: {} to user: {} - Error: {}",
                snippetId,
                jwt.subject,
                sharedUserId,
                e.message,
                e,
            )
            ResponseEntity.status(500).build()
        }
    }

    @DeleteMapping("/{snippetId}")
    fun deleteSnippet(
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Void> {
        logger.info("User: {} attempting to delete snippet: {}", jwt.subject, snippetId)
        return try {
            snippetService.deleteSnippet(snippetId, jwt.subject)
            logger.info("Successfully deleted snippet: {} by user: {}", snippetId, jwt.subject)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid argument when deleting snippet: {} - Error: {}", snippetId, e.message)
            ResponseEntity.badRequest().build()
        } catch (e: AccessDeniedException) {
            logger.warn("Access denied: User {} cannot delete snippet: {}", jwt.subject, snippetId)
            ResponseEntity.status(403).build()
        } catch (e: Exception) {
            logger.error("Unexpected error deleting snippet: {} by user: {} - Error: {}", snippetId, jwt.subject, e.message, e)
            ResponseEntity.status(500).build()
        }
    }
}
