package org.gudelker.snippet.service.modules.snippets

import jakarta.transaction.Transactional
import org.gudelker.snippet.service.api.AssetApiClient
import org.gudelker.snippet.service.api.AuthApiClient
import org.gudelker.snippet.service.api.ResultType
import org.gudelker.snippet.service.modules.langver.LanguageVersionRepository
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
import org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromEditorInput
import org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromFileInput
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
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
    private val languageVersionRepository: LanguageVersionRepository,
) {
    fun getAllSnippets(): List<Snippet> {
        val snippets = snippetRepository.findAll()
        // Initialize lazy-loaded relationships to avoid serialization issues
        snippets.forEach { it.languageVersion.language.name }
        return snippets
    }

    fun createSnippetFromFile(
        input: CreateSnippetFromFileInput,
        jwt: Jwt,
    ): SnippetFromFileResponse {
        val userId = jwt.subject
        try {
            val parseRequest =
                ParseSnippetRequest(
                    snippetContent = input.content,
                    version = input.version,
                )
            val result = authApiClient.parseSnippet(parseRequest)
            if (result == ResultType.FAILURE) {
                throw IllegalArgumentException("Snippet parsing failed")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
        val languageVersion =
            languageVersionRepository.findByLanguageNameAndVersion(input.language, input.version)
                ?: throw IllegalArgumentException("Language version not found")

        val snippet =
            Snippet(
                ownerId = userId,
                title = input.title,
                languageVersion = languageVersion,
                description = input.description,
                created = OffsetDateTime.now(),
                updated = OffsetDateTime.now(),
            )
        val saved = snippetRepository.save(snippet)
        val authorizeRequest = createAuthorizeRequestDto(userId, PermissionType.WRITE)

        try {
            if (saved.id == null) {
                throw RuntimeException("Failed to save snippet")
                println("Hola")
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
        return createSnippetFromFileResponse(input, userId)
    }

    fun getSnippetsByUserId(userId: String): List<Snippet> {
        val snippets = snippetRepository.findByOwnerId(userId)
        // Initialize lazy-loaded relationships to avoid serialization issues
        snippets.forEach { it.languageVersion.language.name }
        return snippets
    }

    fun updateSnippetFromFile(input: UpdateSnippetFromFileInput): UpdateSnippetFromFileResponse {
        if (input.title == null && input.content == null) {
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
        snippet.updated = OffsetDateTime.now()
        val languageVersion =
            languageVersionRepository.findByLanguageNameAndVersion(
                snippet.languageVersion.language.name,
                snippet.languageVersion.version,
            )
        if (languageVersion == null) {
            throw IllegalArgumentException("LanguageVersion not found")
        }

        snippetRepository.save(snippet)
        if (input.content != null) {
            return UpdateSnippetFromFileResponse(
                snippetId = snippet.id.toString(),
                title = snippet.title,
                content = input.content,
                language = languageVersion.language.name,
                version = languageVersion.version,
                updated = snippet.updated,
            )
        }
        return UpdateSnippetFromFileResponse(
            snippetId = snippet.id.toString(),
            title = snippet.title,
            content = assetApiClient.getAsset("snippets", snippet.id.toString()),
            language = languageVersion.language.name,
            version = languageVersion.version,
            updated = snippet.updated,
        )
    }

    fun getSnippetById(snippetId: String): Snippet {
        val snippet =
            snippetRepository.findById(UUID.fromString(snippetId))
                .orElseThrow { RuntimeException("Snippet not found") }
        // Initialize lazy-loaded relationships to avoid serialization issues
        snippet.languageVersion.language.name
        return snippet
    }

    @Transactional
    fun createSnippetFromEditor(
        input: CreateSnippetFromEditor,
        jwt: Jwt,
    ): Snippet {
        val userId = jwt.subject
        val authorizeRequest = createAuthorizeRequestDto(userId, PermissionType.WRITE)

        try {
            val parseRequest =
                ParseSnippetRequest(
                    snippetContent = input.content,
                    version = input.version,
                )
            val result = authApiClient.parseSnippet(parseRequest)
            if (result == ResultType.FAILURE) {
                throw IllegalArgumentException("Snippet parsing failed")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
        val snippet =
            Snippet(
                ownerId = userId,
                title = input.title,
                description = input.description,
                created = OffsetDateTime.now(),
                updated = OffsetDateTime.now(),
                languageVersion =
                    languageVersionRepository.findByLanguageNameAndVersion(input.language, input.version)
                        ?: throw IllegalArgumentException("LanguageVersion not found"),
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
        // Initialize lazy-loaded relationships to avoid serialization issues
        saved.languageVersion.language.name
        return saved
    }

    fun updateSnippetFromEditor(
        input: UpdateSnippetFromEditorInput,
        jwt: Jwt,
    ): UpdateSnippetFromEditorResponse {
        if (input.title == null && input.content == null && input.description == null) {
            throw IllegalArgumentException(
                "At least one attribute (title, content, language, description) must be provided for update.",
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

        if (input.content != null) {
            val parseRequest =
                ParseSnippetRequest(
                    snippetContent = input.content,
                    version = snippet.languageVersion.version,
                )
            val parseResult = authApiClient.parseSnippet(parseRequest)
            if (parseResult == ResultType.FAILURE) {
                throw IllegalArgumentException("Snippet parsing failed")
            }
        }

        input.title?.let { snippet.title = it }
        input.description?.let { snippet.description = it }
        snippet.updated = OffsetDateTime.now()

        snippetRepository.save(snippet)
        return UpdateSnippetFromEditorResponse(
            snippetId = snippet.id.toString(),
            title = snippet.title,
            description = snippet.description,
            content = input.content,
            language = snippet.languageVersion.language.name,
            version = snippet.languageVersion.version,
            updated = snippet.updated,
        )
    }

    fun shareSnippet(
        sharedUserId: String,
        snippetId: UUID,
        userId: String,
    ): ShareSnippetResponseDto {
        val snippet =
            snippetRepository.findById(snippetId)
                .orElseThrow { RuntimeException("Snippet not found") }
        if (snippet.ownerId != userId) {
            throw AccessDeniedException("Only the owner can share the snippet")
        }
        val authorizeRequest = createAuthorizeRequestDto(sharedUserId, PermissionType.READ)
        authApiClient.authorizeSnippet(snippetId, authorizeRequest)
        return ShareSnippetResponseDto(
            sharedUserId = sharedUserId,
            userId = userId,
            snippetId = snippetId,
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

    private fun createSnippetFromFileResponse(
        input: CreateSnippetFromFileInput,
        userId: String,
    ): SnippetFromFileResponse {
        return SnippetFromFileResponse(input.title, input.content, userId)
    }

    fun getSnippetsByFilter(
        jwt: Jwt,
        page: Int,
        pageSize: Int,
        accessType: AccessType,
        name: String,
        language: String,
        passedLint: Boolean,
        sortBy: SortByType,
        direction: DirectionType,
    ): Page<Snippet> {
        val userId = jwt.subject

        if (userId.isEmpty()) {
            throw HttpClientErrorException(HttpStatus.FORBIDDEN, "User ID is missing in JWT")
        }

        // Llamar al servicio de autorización con el enum convertido a string
        val snippetIdsByAccessType =
            try {
                authApiClient.getSnippetsByAccessType(userId, accessType.name)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

        if (snippetIdsByAccessType.isEmpty()) {
            return PageImpl(emptyList(), PageRequest.of(page, pageSize), 0)
        }

        val snippets = snippetRepository.findAllById(snippetIdsByAccessType)

        val userLintRules = lintConfigService.getAllRulesFromUser(userId)

        val filtered =
            snippets.filter { snippet ->
                val passesAllRules =
                    if (userLintRules.isEmpty()) {
                        true
                    } else {
                        userLintRules.all { lintConfig ->
                            lintResultService.snippetPassesRule(snippet.id.toString(), lintConfig.lintRule?.id.toString())
                        }
                    }

                (name.isEmpty() || snippet.title.contains(name, ignoreCase = true)) &&
                    (language.isEmpty() || snippet.languageVersion.language.name.equals(language, ignoreCase = true)) &&
                    (!passedLint || passesAllRules)
            }

        val sorted =
            when (sortBy) {
                SortByType.NAME -> filtered.sortedBy { it.title }
                SortByType.LANGUAGE -> filtered.sortedBy { it.languageVersion.language.name }
                SortByType.PASSED_LINT ->
                    filtered.sortedBy { snippet ->
                        if (userLintRules.isEmpty()) {
                            true
                        } else {
                            userLintRules.all { lintConfig ->
                                lintResultService.snippetPassesRule(snippet.id.toString(), lintConfig.lintRule?.id.toString())
                            }
                        }
                    }
            }

        val ordered = if (direction == DirectionType.DESC) sorted.reversed() else sorted

        val start = page * pageSize
        val end = minOf(start + pageSize, ordered.size)
        val paginatedContent = if (start < ordered.size) ordered.subList(start, end) else emptyList()

        return PageImpl(paginatedContent, PageRequest.of(page, pageSize), ordered.size.toLong())
    }
}
