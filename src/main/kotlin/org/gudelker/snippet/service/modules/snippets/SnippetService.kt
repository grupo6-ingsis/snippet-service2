package org.gudelker.snippet.service.modules.snippets

import jakarta.transaction.Transactional
import org.gudelker.snippet.service.api.AssetApiClient
import org.gudelker.snippet.service.api.AuthApiClient
import org.gudelker.snippet.service.api.EngineApiClient
import org.gudelker.snippet.service.api.ResultType
import org.gudelker.snippet.service.modules.langver.LanguageVersionRepository
import org.gudelker.snippet.service.modules.linting.LintingOrchestratorService
import org.gudelker.snippet.service.modules.linting.lintconfig.LintConfigService
import org.gudelker.snippet.service.modules.linting.lintresult.LintResultService
import org.gudelker.snippet.service.modules.snippets.dto.ParseSnippetRequest
import org.gudelker.snippet.service.modules.snippets.dto.PermissionType
import org.gudelker.snippet.service.modules.snippets.dto.authorization.AuthorizeRequestDto
import org.gudelker.snippet.service.modules.snippets.dto.get.SnippetWithComplianceDto
import org.gudelker.snippet.service.modules.snippets.dto.share.ShareSnippetResponseDto
import org.gudelker.snippet.service.modules.snippets.dto.types.AccessType
import org.gudelker.snippet.service.modules.snippets.dto.types.ComplianceType
import org.gudelker.snippet.service.modules.snippets.dto.types.DirectionType
import org.gudelker.snippet.service.modules.snippets.dto.types.SortByType
import org.gudelker.snippet.service.modules.snippets.dto.update.UpdateSnippetFromEditorResponse
import org.gudelker.snippet.service.modules.snippets.input.create.CreateSnippetFromEditor
import org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromEditorInput
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
    private val engineApiClient: EngineApiClient,
    private val lintConfigService: LintConfigService,
    private val lintResultService: LintResultService,
    private val languageVersionRepository: LanguageVersionRepository,
    private val orchestratorLintingService: LintingOrchestratorService,
) {
    fun getAllSnippets(): List<Snippet> {
        val snippets = snippetRepository.findAll()
        // Initialize lazy-loaded relationships to avoid serialization issues
        snippets.forEach { it.languageVersion.language.name }
        return snippets
    }

    fun getSnippetsByUserId(userId: String): List<Snippet> {
        val snippets = snippetRepository.findByOwnerId(userId)
        // Initialize lazy-loaded relationships to avoid serialization issues
        snippets.forEach { it.languageVersion.language.name }
        return snippets
    }

    fun getSnippetById(snippetId: String): SnippetWithComplianceDto {
        val snippetUuid = UUID.fromString(snippetId)
        val snippet =
            snippetRepository.findById(snippetUuid)
                .orElseThrow { RuntimeException("Snippet not found") }
        val compliance = lintResultService.getSnippetLintComplianceType(snippetUuid)
        // Initialize lazy-loaded relationships to avoid serialization issues
        snippet.languageVersion.language.name
        return SnippetWithComplianceDto(
            id = snippetId,
            title = snippet.title,
            description = snippet.description,
            ownerId = snippet.ownerId,
            languageVersion = snippet.languageVersion,
            created = snippet.created,
            updated = snippet.updated,
            compliance = compliance!!,
        )
    }

    @Transactional
    fun createSnippetFromEditor(
        input: CreateSnippetFromEditor,
        jwt: Jwt,
    ): Snippet {
        val userId = jwt.subject
        val authorizeRequest = createAuthorizeRequestDto(userId, PermissionType.WRITE)

        try {
            parseAndValidateSnippet(input)
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
        val (saved, snippetId) = createAndSaveSnippet(input, userId)
        try {
            authApiClient.authorizeSnippet(snippetId, authorizeRequest)
        } catch (ex: Exception) {
            throw RuntimeException("Authorization failed", ex)
        }
        try {
            assetApiClient.createAsset("snippets", saved.id.toString(), input.content)
        } catch (ex: Exception) {
            throw RuntimeException("Failed to save content", ex)
        }
        orchestratorLintingService.lintSingleSnippet(snippetId, userId)
        // Initialize lazy-loaded relationships to avoid serialization issues
        saved.languageVersion.language.name
        return saved
    }

    fun updateSnippetFromEditor(
        input: UpdateSnippetFromEditorInput,
        jwt: Jwt,
        snippetId: String,
    ): UpdateSnippetFromEditorResponse {
        val snippet = validateAndGetSnippet(input, snippetId, snippetRepository)
        val userId = jwt.subject
        try {
            checkWritePermission(authApiClient, snippetId, userId)
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw ex
        }
        parseAndUpdateSnippet(input, snippet, authApiClient)
        snippetRepository.save(snippet)
        orchestratorLintingService.lintSingleSnippet(UUID.fromString(snippetId), userId)
        updateSnippetAsset(assetApiClient, snippetId, input.content)
        return getUpdateSnippetFromEditorResponse(snippet, input)
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
        )
    }

    fun getSnippetsByFilter(
        jwt: Jwt,
        page: Int,
        pageSize: Int,
        accessType: AccessType,
        name: String,
        language: String,
        passedLint: Boolean?,
        sortBy: SortByType,
        direction: DirectionType,
    ): Page<SnippetWithComplianceDto> {
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
                val matchesName = name.isEmpty() || snippet.title.contains(name, ignoreCase = true)
                val matchesLanguage = language.isEmpty() || snippet.languageVersion.language.name.equals(language, ignoreCase = true)

                // Filtro de linting:
                // - Si passedLint es null (no se pasó el parámetro), no filtramos por lint (todos pasan)
                // - Si passedLint es true, solo mostramos snippets que pasaron el linting
                // - Si passedLint es false, mostramos solo los que NO pasaron el linting
                val matchesLintFilter =
                    when (passedLint) {
                        null -> true // No filtrar por lint
                        true -> userLintRules.isEmpty() || lintResultService.snippetPassesLinting(snippet.id.toString())
                        false -> userLintRules.isNotEmpty() && !lintResultService.snippetPassesLinting(snippet.id.toString())
                    }

                matchesName && matchesLanguage && matchesLintFilter
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
                            lintResultService.snippetPassesLinting(snippet.id.toString())
                        }
                    }
            }

        val ordered = if (direction == DirectionType.DESC) sorted.reversed() else sorted
        val snippetsWithCompliance =
            ordered.map { snippet ->
                val complianceType =
                    if (userLintRules.isEmpty()) {
                        ComplianceType.COMPLIANT
                    } else if (lintResultService.snippetPassesLinting(snippet.id.toString())) {
                        ComplianceType.COMPLIANT
                    } else {
                        ComplianceType.NON_COMPLIANT
                    }
                SnippetWithComplianceDto(
                    id = snippet.id.toString(),
                    title = snippet.title,
                    description = snippet.description,
                    ownerId = snippet.ownerId,
                    languageVersion = snippet.languageVersion,
                    created = snippet.created,
                    updated = snippet.updated,
                    compliance = complianceType,
                )
            }
        val start = page * pageSize
        val end = minOf(start + pageSize, snippetsWithCompliance.size)
        val paginatedContent = if (start < ordered.size) snippetsWithCompliance.subList(start, end) else emptyList()

        return PageImpl(paginatedContent, PageRequest.of(page, pageSize), ordered.size.toLong())
    }

    @Transactional
    fun deleteSnippet(
        snippetId: String,
        userId: String,
    ) {
        val snippetUUID =
            try {
                UUID.fromString(snippetId)
            } catch (ex: Exception) {
                throw IllegalArgumentException("Invalid snippetId format: $snippetId", ex)
            }
        val snippet =
            snippetRepository.findById(snippetUUID)
                .orElseThrow { RuntimeException("Snippet not found") }

        if (snippet.ownerId != userId) {
            throw AccessDeniedException("Only the owner can delete the snippet")
        }

        try {
            assetApiClient.deleteAsset("snippets", snippetId)
        } catch (ex: Exception) {
            println("Warning: Failed to delete asset: ${ex.message}")
        }

        snippetRepository.delete(snippet)
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

    private fun createParseSnippetRequest(input: CreateSnippetFromEditor): ParseSnippetRequest {
        return ParseSnippetRequest(
            snippetContent = input.content,
            version = input.version,
        )
    }

    private fun parseAndValidateSnippet(input: CreateSnippetFromEditor) {
        val parseRequest = createParseSnippetRequest(input)
        val result = engineApiClient.parseSnippet(parseRequest)
        if (result == ResultType.FAILURE) {
            throw IllegalArgumentException("Snippet parsing failed")
        }
    }

    private fun createSnippetFromEditorInput(
        input: CreateSnippetFromEditor,
        userId: String,
    ): Snippet {
        return Snippet(
            ownerId = userId,
            title = input.title,
            description = input.description,
            created = OffsetDateTime.now(),
            updated = OffsetDateTime.now(),
            languageVersion =
                languageVersionRepository.findByLanguageNameAndVersion(input.language, input.version)
                    ?: throw IllegalArgumentException("LanguageVersion not found"),
        )
    }

    private fun createAndSaveSnippet(
        input: CreateSnippetFromEditor,
        userId: String,
    ): Pair<Snippet, UUID> {
        val snippet = createSnippetFromEditorInput(input, userId)
        val saved = snippetRepository.save(snippet)
        val snippetId = saved.id ?: throw RuntimeException("Failed to save snippet")
        return Pair(saved, snippetId)
    }

    private fun validateAndGetSnippet(
        input: UpdateSnippetFromEditorInput,
        snippetId: String,
        snippetRepository: SnippetRepository,
    ): Snippet {
        if (input.title == null && input.content == null && input.description == null) {
            throw IllegalArgumentException(
                "At least one attribute (title, content, language, description) must be provided for update.",
            )
        }
        val uuid = UUID.fromString(snippetId)
        return snippetRepository.findById(uuid)
            .orElseThrow { RuntimeException("Snippet not found") }
    }

    private fun checkWritePermission(
        authApiClient: AuthApiClient,
        snippetId: String,
        userId: String,
    ) {
        val isAuthorized = authApiClient.isUserAuthorizedToWriteSnippet(snippetId, userId)
        if (!isAuthorized) {
            throw RuntimeException("User does not have WRITE permission for this snippet")
        }
    }

    private fun parseAndUpdateSnippet(
        input: UpdateSnippetFromEditorInput,
        snippet: Snippet,
        authApiClient: AuthApiClient,
    ) {
        if (input.content != null) {
            val parseRequest =
                ParseSnippetRequest(
                    snippetContent = input.content,
                    version = snippet.languageVersion.version,
                )
            val parseResult = engineApiClient.parseSnippet(parseRequest)
            if (parseResult == ResultType.FAILURE) {
                throw IllegalArgumentException("Snippet parsing failed")
            }
        }
        input.title?.let { snippet.title = it }
        input.description?.let { snippet.description = it }
        snippet.updated = OffsetDateTime.now()
    }

    private fun updateSnippetAsset(
        assetApiClient: AssetApiClient,
        snippetId: String,
        content: String?,
    ) {
        if (content != null) {
            try {
                assetApiClient.updateAsset("snippets", snippetId, content)
            } catch (ex: Exception) {
                throw RuntimeException("Failed to update content", ex)
            }
        }
    }

    private fun getUpdateSnippetFromEditorResponse(
        snippet: Snippet,
        input: UpdateSnippetFromEditorInput,
    ): UpdateSnippetFromEditorResponse {
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
}
