package org.gudelker.snippet.service.modules.snippets

import jakarta.transaction.Transactional
import org.gudelker.snippet.service.api.AssetApiClient
import org.gudelker.snippet.service.api.AuthApiClient
import org.gudelker.snippet.service.api.ResultType
import org.gudelker.snippet.service.modules.lintconfig.LintConfigService
import org.gudelker.snippet.service.modules.lintresult.LintResultService
import org.gudelker.snippet.service.modules.snippets.dto.ParseSnippetRequest
import org.gudelker.snippet.service.modules.snippets.dto.PermissionType
import org.gudelker.snippet.service.modules.snippets.dto.authorization.AuthorizeRequestDto
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
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.time.OffsetDateTime
import java.util.UUID

@Service
class SnippetService(
    private val snippetRepository: SnippetRepository,
    private val authApiClient: AuthApiClient,
    private val assetApiClient: AssetApiClient,
    private val lintConfigService: LintConfigService,
    private val lintResultService: LintResultService,
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
            jwt.subject
        val request = createAuthorizeRequestDto(userId, PermissionType.WRITE)
        try {
            authApiClient.authorizeSnippet(snippetId, request)
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
        val content = input.content
        val container = "snippets"
        assetApiClient.createAsset(container, snippetId.toString(), content)

        val snippet = createSnippet(snippetId, userId, input)
        snippetRepository.save(snippet)
        return createSnippetFromFileResponse(input, userId)
    }

    fun getSnippetsByUserId(userId: String): List<Snippet> {
        return snippetRepository.findByOwnerId(userId)
    }

    fun updateSnippetFromFile(input: UpdateSnippetFromFileInput): UpdateSnippetFromFileResponse {
        if (input.title == null && input.content == null && input.language == null) {
            throw IllegalArgumentException("At least one attribute (title, content, language) must be provided for update.")
        }
        val snippet =
            snippetRepository.findById(input.snippetId)
                .orElseThrow { RuntimeException("Snippet not found") }

        val authorization = authApiClient.authorizeUpdateSnippet(input.snippetId)
        if (!authorization) {
            throw RuntimeException("Authorization failed")
        }

        input.title?.let { snippet.title = it }
        input.language?.let { snippet.language = it }
        snippet.updated = OffsetDateTime.now()

        snippetRepository.save(snippet)
        if (input.content != null) {
            return UpdateSnippetFromFileResponse(
                snippetId = snippet.id.toString(),
                title = snippet.title,
                content = input.content,
                language = snippet.language,
                updated = snippet.updated,
            )
        }
        return UpdateSnippetFromFileResponse(
            snippetId = snippet.id.toString(),
            title = snippet.title,
            content = assetApiClient.getAsset("snippets", snippet.id.toString()),
            language = snippet.language,
            updated = snippet.updated,
        )
    }

    fun getSnippetById(snippetId: String): Snippet {
        return snippetRepository.findById(UUID.fromString(snippetId))
            .orElseThrow { RuntimeException("Snippet not found") }
    }

    @Transactional
    fun createSnippetFromEditor(
        input: CreateSnippetFromEditor,
        jwt: Jwt,
    ): Snippet {
        val userId = jwt.subject
        println(userId)
        val authorizeRequest = createAuthorizeRequestDto(userId, PermissionType.WRITE)

//        try {
//            val parseRequest =
//                ParseSnippetRequest(
//                    snippetContent = input.content,
//                    version = input.version,
//                )
//            val result = authApiClient.parseSnippet(parseRequest)
//            if (result == ResultType.FAILURE) {
//                throw IllegalArgumentException("Snippet parsing failed")
//            }
//        } catch (ex: Exception) {
//            ex.printStackTrace()
//            throw ex
//        }

        val snippet =
            Snippet(
                ownerId = userId,
                title = input.title,
                language = input.language,
                description = input.description,
                snippetVersion = input.version,
                created = OffsetDateTime.now(),
                updated = OffsetDateTime.now(),
            )
        val saved = snippetRepository.save(snippet)

        try {
            if (saved.id == null) {
                throw RuntimeException("Failed to save snippet")
            }
            authApiClient.authorizeSnippet(saved.id!!, authorizeRequest)
        } catch (ex: Exception) {
            throw RuntimeException("Authorization failed", ex)
        }
        try {
            assetApiClient.createAsset("snippets", saved.id.toString(), input.content)
        } catch (ex: Exception) {
            throw RuntimeException("Failed to save content", ex)
        }
        return snippet
    }

    fun updateSnippetFromEditor(
        input: UpdateSnippetFromEditorInput,
        jwt: Jwt,
    ): UpdateSnippetFromEditorResponse {
        if (input.title == null && input.content == null && input.language == null && input.description == null && input.version == null) {
            throw IllegalArgumentException(
                "At least one attribute (title, content, language, description, version) must be provided for update.",
            )
        }
        val snippetId = UUID.fromString(input.snippetId)
        val snippet =
            snippetRepository.findById(snippetId)
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
            val parseRequest =
                ParseSnippetRequest(
                    snippetContent = input.content,
                    version = input.version,
                )
            val parseResult = authApiClient.parseSnippet(parseRequest)
            if (parseResult == ResultType.FAILURE) {
                throw IllegalArgumentException("Snippet parsing failed")
            }
        }

        input.title?.let { snippet.title = it }
        input.description?.let { snippet.description = it }
        input.language?.let { snippet.language = it }
        input.version?.let { snippet.snippetVersion = it }
        snippet.updated = OffsetDateTime.now()

        snippetRepository.save(snippet)
        return UpdateSnippetFromEditorResponse(
            snippetId = snippet.id.toString(),
            title = snippet.title,
            description = snippet.description,
            content = input.content,
            language = snippet.language,
            version = snippet.snippetVersion.toString(),
            updated = snippet.updated,
        )
    }

    fun shareSnippet(
        input: ShareSnippetInput,
        userId: String,
    ): ShareSnippetResponseDto {
        val snippet =
            snippetRepository.findById(input.snippetId)
                .orElseThrow { RuntimeException("Snippet not found") }
        if (snippet.ownerId != userId) {
            throw AccessDeniedException("Only the owner can share the snippet")
        }
        val authorizeRequest = createAuthorizeRequestDto(input.sharedId, PermissionType.READ)
        authApiClient.authorizeSnippet(input.snippetId, authorizeRequest)
        return ShareSnippetResponseDto(
            sharedUserId = input.sharedId,
            userId = userId,
            snippetId = input.snippetId,
            permissionType = PermissionType.READ,
        )
    }

    private fun createAuthorizeRequestDto(
        userId: String,
        permission: PermissionType,
    ): AuthorizeRequestDto {
        return AuthorizeRequestDto(
            userId = userId,
            permission = permission,
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
            language = input.language,
            snippetVersion = input.version,
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
        direction: DirectionType,
    ): List<Snippet> {
        val userId = jwt.subject
        if (userId.isEmpty()) {
            throw HttpClientErrorException(HttpStatus.FORBIDDEN, "User ID is missing in JWT")
        }

        val snippetIdsByAccessType = authApiClient.getSnippetsByAccessType(userId, accessType.name)
        val snippets = snippetRepository.findAllById(snippetIdsByAccessType)

        val userLintRules = lintConfigService.getAllRulesFromUser(userId)

        val filtered =
            snippets.filter { snippet ->
                val passesAllRules = snippetPassesAllRules(snippet, userLintRules, lintResultService)

                (name.isEmpty() || snippet.title.contains(name, ignoreCase = true)) &&
                    (language.isEmpty() || snippet.language.equals(language, ignoreCase = true)) &&
                    (!passedLint || passesAllRules)
            }

        val sorted =
            when (sortBy) {
                SortByType.NAME -> filtered.sortedBy { it.title }
                SortByType.LANGUAGE -> filtered.sortedBy { it.language }
                SortByType.PASSED_LINT ->
                    filtered.sortedBy { snippet ->
                        snippetPassesAllRules(snippet, userLintRules, lintResultService)
                    }
            }

        return if (direction == DirectionType.DESC) sorted.reversed() else sorted
    }
}
