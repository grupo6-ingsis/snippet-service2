package org.gudelker.snippet.service.modules.snippets

import org.gudelker.snippet.service.api.AuthApiClient
import org.gudelker.snippet.service.api.ResultType
import org.gudelker.snippet.service.modules.lint_config.LintConfig
import org.gudelker.snippet.service.modules.lint_config.LintConfigService
import org.gudelker.snippet.service.modules.lint_result.LintResultRepository
import org.gudelker.snippet.service.modules.lint_result.LintResultService
import org.gudelker.snippet.service.modules.snippets.dto.authorization.AuthorizeRequestDto
import org.gudelker.snippet.service.modules.snippets.dto.create.SnippetFromFileResponse
import org.gudelker.snippet.service.modules.snippets.dto.update.UpdateSnippetFromFileResponse
import org.gudelker.snippet.service.modules.snippets.input.create.CreateSnippetFromFileInput
import org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromFileInput
import org.gudelker.snippet.service.modules.snippets.dto.ParseSnippetRequest
import org.gudelker.snippet.service.modules.snippets.dto.PermissionType
import org.gudelker.snippet.service.modules.snippets.dto.types.AccessType
import org.gudelker.snippet.service.modules.snippets.dto.types.DirectionType
import org.gudelker.snippet.service.modules.snippets.dto.types.SortByType
import org.gudelker.snippet.service.modules.snippets.dto.update.UpdateSnippetFromEditorResponse
import org.gudelker.snippet.service.modules.snippets.input.create.CreateSnippetFromEditor
import org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromEditorInput
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.time.OffsetDateTime
import java.util.UUID

@Service
class SnippetService(
    private val snippetRepository: SnippetRepository,
    private val authApiClient: AuthApiClient,
    private val lintConfigService: LintConfigService,
    private val lintResultService: LintResultService
) {
    fun getAllSnippets(): List<Snippet> {
        return snippetRepository.findAll()
    }

    fun createSnippetFromFile(
        input: CreateSnippetFromFileInput,
        jwt: Jwt,
    ): SnippetFromFileResponse {
        val snippetId = UUID.randomUUID()
        val userId =
            jwt.claims["sub"] as? String
                ?: throw IllegalArgumentException("JWT missing 'sub' claim")
        val request = createAuthorizeRequestDto(userId, listOf(PermissionType.OWNER))
        try {
            authApiClient.authorizeSnippet(snippetId, request)
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
        val snippet = createSnippet(snippetId, userId, input)
        snippetRepository.save(snippet)
        return createSnippetFromFileResponse(input, userId)
    }

    fun getSnippetsByUserId(userId: String): List<Snippet> {
        return snippetRepository.findByOwnerId(userId)
    }

    fun updateSnippetFromFile(
        input: UpdateSnippetFromFileInput,
    ): UpdateSnippetFromFileResponse {
        if (input.title == null && input.content == null && input.language == null) {
            throw IllegalArgumentException("At least one attribute (title, content, language) must be provided for update.")
        }
        val snippetId = UUID.fromString(input.snippetId)
        val snippet =
            snippetRepository.findById(snippetId)
                .orElseThrow { RuntimeException("Snippet not found") }

        val authorization = authApiClient.authorizeUpdateSnippet(input.snippetId)
        if (!authorization) {
            throw RuntimeException("Authorization failed")
        }

        input.title?.let { snippet.title = it }
        input.content?.let { snippet.content = it }
        input.language?.let { snippet.language = it }
        snippet.updated = OffsetDateTime.now()

        snippetRepository.save(snippet)
        return UpdateSnippetFromFileResponse(
            snippetId = snippet.id.toString(),
            title = snippet.title,
            content = snippet.content,
            language = snippet.language,
            updated = snippet.updated,
        )
    }

    fun getSnippetById(snippetId: String): Snippet {
        return snippetRepository.findById(UUID.fromString(snippetId))
            .orElseThrow { RuntimeException("Snippet not found") }
    }

    fun createSnippetFromEditor(input: CreateSnippetFromEditor, jwt: Jwt): Snippet {
        val snippetId = UUID.randomUUID()
        val userId = jwt.subject
        val request = createAuthorizeRequestDto(userId, listOf(PermissionType.OWNER))
        try {
            val request = ParseSnippetRequest(
                snippetContent = input.content,
                version = input.version,
            )
            val result = authApiClient.parseSnippet(request)
            if (result == ResultType.FAILURE) {
                throw IllegalArgumentException("Snippet parsing failed")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }

        val snippet = Snippet(
            id = snippetId,
            ownerId = userId,
            title = input.title,
            content = input.content,
            language = input.language,
            version = input.version,
            created = OffsetDateTime.now(),
            updated = OffsetDateTime.now(),
        )
        snippetRepository.save(snippet)
        try {
            authApiClient.authorizeSnippet(snippetId, request)
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
        return snippet
    }

    fun updateSnippetFromEditor(
        input: UpdateSnippetFromEditorInput,
        jwt: Jwt
    ): UpdateSnippetFromEditorResponse {
        if (input.title == null && input.content == null && input.language == null && input.description == null && input.version == null) {
            throw IllegalArgumentException("At least one attribute (title, content, language, description, version) must be provided for update.")
        }
        val snippetId = UUID.fromString(input.snippetId)
        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { RuntimeException("Snippet not found") }

        val userId = jwt.subject
        try {
            val isAuthorized = authApiClient.isUserAuthorizedToWriteSnippet(snippetId.toString(), userId)
            if (!isAuthorized) {
                throw RuntimeException("User does not have WRITE permission for this snippet")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }

        if (input.content != null && input.version != null) {
            val parseRequest = ParseSnippetRequest(
                snippetContent = input.content,
                version = input.version
            )
            val parseResult = authApiClient.parseSnippet(parseRequest)
            if (parseResult == ResultType.FAILURE) {
                throw IllegalArgumentException("Snippet parsing failed")
            }
        }

        input.title?.let { snippet.title = it }
        input.description?.let { snippet.description = it }
        input.content?.let { snippet.content = it }
        input.language?.let { snippet.language = it }
        input.version?.let { snippet.version = it }
        snippet.updated = OffsetDateTime.now()

        snippetRepository.save(snippet)
        return UpdateSnippetFromEditorResponse(
            snippetId = snippet.id.toString(),
            title = snippet.title,
            description = snippet.description,
            content = snippet.content,
            language = snippet.language,
            version = snippet.version.toString(),
            updated = snippet.updated,
        )
    }

    private fun createAuthorizeRequestDto(
        userId: String,
        permissions: List<PermissionType>,
    ): AuthorizeRequestDto {
        return AuthorizeRequestDto(
            userId = userId,
            permissions = permissions,
        )
    }

    private fun createSnippet(
        id: UUID,
        ownerId: String,
        input: CreateSnippetFromFileInput,
    ): Snippet {
        return Snippet(
            id = id,
            ownerId = ownerId,
            title = input.title,
            content = input.content,
            language = input.language,
            version = input.version,
            created = OffsetDateTime.now(),
            updated = OffsetDateTime.now(),
        )
    }

    private fun createSnippetFromFileResponse(
        input: CreateSnippetFromFileInput,
        userId: String,
    ): SnippetFromFileResponse {
        return SnippetFromFileResponse(input.title, input.content, userId)
    }

    fun getSnippetsByFilter(
        jwt: Jwt,
        accessType: AccessType,
        name: String,
        language: String,
        passedLint: Boolean,
        sortBy: SortByType,
        direction: DirectionType
    ): List<Snippet> {
        val userId = jwt.subject
        if (userId.isEmpty()) {
            throw HttpClientErrorException(HttpStatus.FORBIDDEN, "User ID is missing in JWT")
        }

        val snippetIdsByAccessType = authApiClient.getSnippetsByAccessType(userId, accessType.name)
        val snippets = snippetRepository.findAllById(snippetIdsByAccessType)

        val userLintRules = lintConfigService.getAllRulesFromUser(userId)

        val filtered = snippets.filter { snippet ->
            val passesAllRules = snippetPassesAllRules(snippet, userLintRules, lintResultService)

            (name.isEmpty() || snippet.title.contains(name, ignoreCase = true)) &&
                    (language.isEmpty() || snippet.language.equals(language, ignoreCase = true)) &&
                    (!passedLint || passesAllRules)
        }

        val sorted = when (sortBy) {
            SortByType.NAME -> filtered.sortedBy { it.title }
            SortByType.LANGUAGE -> filtered.sortedBy { it.language }
            SortByType.PASSED_LINT -> filtered.sortedBy { snippet ->
                snippetPassesAllRules(snippet, userLintRules, lintResultService)
            }
        }

        return if (direction == DirectionType.DESC) sorted.reversed() else sorted
    }


}