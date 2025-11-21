package org.gudelker.snippet.service.modules.snippets

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gudelker.snippet.service.api.AssetApiClient
import org.gudelker.snippet.service.api.AuthApiClient
import org.gudelker.snippet.service.api.EngineApiClient
import org.gudelker.snippet.service.api.ResultType
import org.gudelker.snippet.service.modules.langver.LanguageVersion
import org.gudelker.snippet.service.modules.langver.LanguageVersionRepository
import org.gudelker.snippet.service.modules.linting.LintingOrchestratorService
import org.gudelker.snippet.service.modules.linting.lintconfig.LintConfig
import org.gudelker.snippet.service.modules.linting.lintconfig.LintConfigService
import org.gudelker.snippet.service.modules.linting.lintresult.LintResultService
import org.gudelker.snippet.service.modules.snippets.dto.types.AccessType
import org.gudelker.snippet.service.modules.snippets.dto.types.DirectionType
import org.gudelker.snippet.service.modules.snippets.dto.types.SortByType
import org.gudelker.snippet.service.modules.snippets.input.create.CreateSnippetFromEditor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.assertEquals

class SnippetServiceABMTest {
    private lateinit var snippetRepository: SnippetRepository
    private lateinit var authApiClient: AuthApiClient
    private lateinit var assetApiClient: AssetApiClient
    private lateinit var engineApiClient: EngineApiClient
    private lateinit var lintConfigService: LintConfigService
    private lateinit var lintResultService: LintResultService
    private lateinit var languageVersionRepository: LanguageVersionRepository
    private lateinit var orchestratorLintingService: LintingOrchestratorService
    private lateinit var snippetService: SnippetService

    @BeforeEach
    fun setUp() {
        snippetRepository = mockk(relaxed = true)
        authApiClient = mockk(relaxed = true)
        assetApiClient = mockk(relaxed = true)
        engineApiClient = mockk(relaxed = true)
        lintConfigService = mockk(relaxed = true)
        lintResultService = mockk(relaxed = true)
        languageVersionRepository = mockk(relaxed = true)
        orchestratorLintingService = mockk(relaxed = true)
        snippetService =
            SnippetService(
                snippetRepository,
                authApiClient,
                assetApiClient,
                engineApiClient,
                lintConfigService,
                lintResultService,
                languageVersionRepository,
                orchestratorLintingService,
            )
    }

    @Test
    fun `createSnippetFromEditor should save and return snippet`() {
        val input =
            CreateSnippetFromEditor(
                title = "Test Title",
                description = "Test Description",
                language = "Printscript",
                content = "let boca = 1;",
                version = "1.0",
            )
        val jwt =
            mockk<Jwt> {
                every { subject } returns "user-123"
            }
        val languageVersion = mockk<LanguageVersion>(relaxed = true)
        val snippet =
            Snippet(
                id = UUID.randomUUID(),
                ownerId = "user-123",
                title = input.title,
                description = input.description,
                languageVersion = languageVersion,
            )
        every { snippetRepository.save(any()) } returns snippet
        every { languageVersionRepository.findByLanguageNameAndVersion(any(), any()) } returns languageVersion
        val result = snippetService.createSnippetFromEditor(input, jwt)
        assertEquals(snippet, result)
        verify { snippetRepository.save(any()) }
        verify { authApiClient.authorizeSnippet(any(), any()) }
        verify { assetApiClient.createAsset(any(), any(), any()) }
        verify { orchestratorLintingService.lintSingleSnippet(any(), any()) }
    }

    @Test
    fun `createSnippetFromEditor should throw when parseAndValidateSnippet fails`() {
        val input =
            CreateSnippetFromEditor(
                title = "Test Title",
                description = "Test Description",
                language = "Printscript",
                content = "let boca = 1;",
                version = "1.0",
            )
        val jwt =
            mockk<Jwt> {
                every { subject } returns "user-123"
            }
        every { engineApiClient.parseSnippet(any()) } returns ResultType.FAILURE
        val languageVersion = mockk<LanguageVersion>(relaxed = true)
        every { languageVersionRepository.findByLanguageNameAndVersion(any(), any()) } returns languageVersion
        try {
            snippetService.createSnippetFromEditor(input, jwt)
            assert(false) { "Expected exception not thrown" }
        } catch (e: IllegalArgumentException) {
            assert(e.message?.contains("Snippet parsing failed") == true)
        }
    }

    @Test
    fun `createSnippetFromEditor should throw when authorization fails`() {
        val input =
            CreateSnippetFromEditor(
                title = "Test Title",
                description = "Test Description",
                language = "Printscript",
                content = "let boca = 1;",
                version = "1.0",
            )
        val jwt =
            mockk<Jwt> {
                every { subject } returns "user-123"
            }
        val languageVersion = mockk<LanguageVersion>(relaxed = true)
        val snippet =
            Snippet(
                id = UUID.randomUUID(),
                ownerId = "user-123",
                title = input.title,
                description = input.description,
                languageVersion = languageVersion,
            )
        every { snippetRepository.save(any()) } returns snippet
        every { languageVersionRepository.findByLanguageNameAndVersion(any(), any()) } returns languageVersion
        every { authApiClient.authorizeSnippet(any(), any()) } throws RuntimeException("Authorization failed")
        try {
            snippetService.createSnippetFromEditor(input, jwt)
            assert(false) { "Expected exception not thrown" }
        } catch (e: RuntimeException) {
            assertEquals("Authorization failed", e.message)
        }
    }

    @Test
    fun `createSnippetFromEditor should throw when asset creation fails`() {
        val input =
            CreateSnippetFromEditor(
                title = "Test Title",
                description = "Test Description",
                language = "Printscript",
                content = "let boca = 1;",
                version = "1.0",
            )
        val jwt =
            mockk<Jwt> {
                every { subject } returns "user-123"
            }
        val languageVersion = mockk<LanguageVersion>(relaxed = true)
        val snippet =
            Snippet(
                id = UUID.randomUUID(),
                ownerId = "user-123",
                title = input.title,
                description = input.description,
                languageVersion = languageVersion,
            )
        every { snippetRepository.save(any()) } returns snippet
        every { languageVersionRepository.findByLanguageNameAndVersion(any(), any()) } returns languageVersion
        every { assetApiClient.createAsset(any(), any(), any()) } throws RuntimeException("Failed to save content")
        try {
            snippetService.createSnippetFromEditor(input, jwt)
            assert(false) { "Expected exception not thrown" }
        } catch (e: RuntimeException) {
            assert(e.message?.contains("Failed to save content") == true)
        }
    }

    @Test
    fun `createSnippetFromEditor should throw when language version not found`() {
        val input =
            CreateSnippetFromEditor(
                title = "Test Title",
                description = "Test Description",
                language = "PrintScript",
                content = "let boca = 1;",
                version = "1.0",
            )
        val jwt =
            mockk<Jwt> {
                every { subject } returns "user-123"
            }
        every { languageVersionRepository.findByLanguageNameAndVersion(any(), any()) } returns null
        try {
            snippetService.createSnippetFromEditor(input, jwt)
            assert(false) { "Expected exception not thrown" }
        } catch (e: IllegalArgumentException) {
            assert(e.message?.contains("LanguageVersion not found") == true)
        }
    }

    @Test
    fun `createSnippetFromEditor should throw when userId is null in JWT`() {
        val input =
            CreateSnippetFromEditor(
                title = "Test Title",
                description = "Test Description",
                language = "Printscript",
                content = "let boca = 1;",
                version = "1.0",
            )
        val jwt =
            mockk<Jwt> {
                every { subject } returns null
            }
        try {
            snippetService.createSnippetFromEditor(input, jwt)
            assert(false) { "Expected exception not thrown" }
        } catch (e: Exception) {
            assert(e is NullPointerException || e.message?.contains("User ID") == true)
        }
    }

    @Test
    fun `createSnippetFromEditor should save asset and getAsset should return matching content`() {
        val input =
            CreateSnippetFromEditor(
                title = "Test Title",
                description = "Test Description",
                language = "Printscript",
                content = "let boca = 1;",
                version = "1.0",
            )
        val jwt =
            mockk<Jwt> {
                every { subject } returns "user-123"
            }
        val languageVersion = mockk<LanguageVersion>(relaxed = true)
        val snippet =
            Snippet(
                id = UUID.randomUUID(),
                ownerId = "user-123",
                title = input.title,
                description = input.description,
                languageVersion = languageVersion,
            )
        every { snippetRepository.save(any()) } returns snippet
        every { languageVersionRepository.findByLanguageNameAndVersion(any(), any()) } returns languageVersion
        every { assetApiClient.createAsset("snippets", snippet.id.toString(), input.content) } returns Unit
        every { assetApiClient.getAsset("snippets", snippet.id.toString()) } returns input.content

        snippetService.createSnippetFromEditor(input, jwt)
        val retrievedContent = assetApiClient.getAsset("snippets", snippet.id.toString())
        assertEquals(input.content, retrievedContent)
        verify { assetApiClient.createAsset("snippets", snippet.id.toString(), input.content) }
        verify { assetApiClient.getAsset("snippets", snippet.id.toString()) }
    }

    @Test
    fun `updateSnippetFromEditor should update snippet and asset successfully`() {
        val snippetId = UUID.randomUUID().toString()
        val jwt = mockk<Jwt> { every { subject } returns "user-123" }
        val languageVersion = mockk<LanguageVersion>(relaxed = true)
        val snippet =
            Snippet(
                id = UUID.fromString(snippetId),
                ownerId = "user-123",
                title = "Old Title",
                description = "Old Desc",
                languageVersion = languageVersion,
            )
        val input =
            org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromEditorInput(
                title = "New Title",
                description = "New Desc",
                content = "new content",
            )
        every { snippetRepository.findById(UUID.fromString(snippetId)) } returns java.util.Optional.of(snippet)
        every { authApiClient.isUserAuthorizedToWriteSnippet(snippetId, "user-123") } returns true
        every { engineApiClient.parseSnippet(any()) } returns ResultType.SUCCESS
        every { snippetRepository.save(any()) } returns snippet
        every { assetApiClient.updateAsset("snippets", snippetId, input.content!!) } returns Unit
        every { orchestratorLintingService.lintSingleSnippet(UUID.fromString(snippetId), "user-123") } returns Unit

        val result = snippetService.updateSnippetFromEditor(input, jwt, snippetId)
        assertEquals("New Title", result.title)
        assertEquals("New Desc", result.description)
        assertEquals("new content", result.content)
        verify { snippetRepository.save(any()) }
        verify { assetApiClient.updateAsset("snippets", snippetId, input.content!!) }
        verify { orchestratorLintingService.lintSingleSnippet(UUID.fromString(snippetId), "user-123") }
    }

    @Test
    fun `updateSnippetFromEditor should throw if not authorized`() {
        val snippetId = UUID.randomUUID().toString()
        val jwt = mockk<Jwt> { every { subject } returns "user-123" }
        val languageVersion = mockk<LanguageVersion>(relaxed = true)
        val snippet =
            Snippet(
                id = UUID.fromString(snippetId),
                ownerId = "user-123",
                title = "Old Title",
                description = "Old Desc",
                languageVersion = languageVersion,
            )
        val input =
            org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromEditorInput(
                title = "New Title",
                description = null,
                content = null,
            )
        every { snippetRepository.findById(UUID.fromString(snippetId)) } returns java.util.Optional.of(snippet)
        every { authApiClient.isUserAuthorizedToWriteSnippet(snippetId, "user-123") } returns false
        try {
            snippetService.updateSnippetFromEditor(input, jwt, snippetId)
            assert(false) { "Expected exception not thrown" }
        } catch (e: RuntimeException) {
            assert(e.message?.contains("WRITE permission") == true)
        }
    }

    @Test
    fun `updateSnippetFromEditor should throw if parsing fails`() {
        val snippetId = UUID.randomUUID().toString()
        val jwt = mockk<Jwt> { every { subject } returns "user-123" }
        val languageVersion = mockk<LanguageVersion>(relaxed = true)
        val snippet =
            Snippet(
                id = UUID.fromString(snippetId),
                ownerId = "user-123",
                title = "Old Title",
                description = "Old Desc",
                languageVersion = languageVersion,
            )
        val input =
            org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromEditorInput(
                title = null,
                description = null,
                content = "bad content",
            )
        every { snippetRepository.findById(UUID.fromString(snippetId)) } returns java.util.Optional.of(snippet)
        every { authApiClient.isUserAuthorizedToWriteSnippet(snippetId, "user-123") } returns true
        every { engineApiClient.parseSnippet(any()) } returns ResultType.FAILURE
        try {
            snippetService.updateSnippetFromEditor(input, jwt, snippetId)
            assert(false) { "Expected exception not thrown" }
        } catch (e: IllegalArgumentException) {
            assert(e.message?.contains("parsing failed") == true)
        }
    }

    @Test
    fun `updateSnippetFromEditor should throw if no fields to update`() {
        val snippetId = UUID.randomUUID().toString()
        val jwt = mockk<Jwt> { every { subject } returns "user-123" }
        val input =
            org.gudelker.snippet.service.modules.snippets.input.update.UpdateSnippetFromEditorInput(
                title = null,
                description = null,
                content = null,
            )
        try {
            snippetService.updateSnippetFromEditor(input, jwt, snippetId)
            assert(false) { "Expected exception not thrown" }
        } catch (e: IllegalArgumentException) {
            assert(e.message?.contains("At least one attribute") == true)
        }
    }

    @Test
    fun `deleteSnippet should delete asset and snippet successfully`() {
        val snippetId = UUID.randomUUID().toString()
        val userId = "user-123"
        val languageVersion = mockk<LanguageVersion>(relaxed = true)
        val snippet =
            Snippet(
                id = UUID.fromString(snippetId),
                ownerId = userId,
                title = "Title",
                description = "Desc",
                languageVersion = languageVersion,
            )
        every { snippetRepository.findById(UUID.fromString(snippetId)) } returns java.util.Optional.of(snippet)
        every { assetApiClient.deleteAsset("snippets", snippetId) } returns Unit
        every { snippetRepository.delete(snippet) } returns Unit

        snippetService.deleteSnippet(snippetId, userId)
        verify { assetApiClient.deleteAsset("snippets", snippetId) }
        verify { snippetRepository.delete(snippet) }
    }

    @Test
    fun `deleteSnippet should throw if not owner`() {
        val snippetId = UUID.randomUUID().toString()
        val userId = "user-123"
        val languageVersion = mockk<LanguageVersion>(relaxed = true)
        val snippet =
            Snippet(
                id = UUID.fromString(snippetId),
                ownerId = "other-user",
                title = "Title",
                description = "Desc",
                languageVersion = languageVersion,
            )
        every { snippetRepository.findById(UUID.fromString(snippetId)) } returns java.util.Optional.of(snippet)
        try {
            snippetService.deleteSnippet(snippetId, userId)
            assert(false) { "Expected exception not thrown" }
        } catch (e: Exception) {
            assert(e.message?.contains("Only the owner") == true)
        }
    }

    @Test
    fun `deleteSnippet should throw if snippetId is invalid`() {
        val invalidId = "not-a-uuid"
        try {
            snippetService.deleteSnippet(invalidId, "user-123")
            assert(false) { "Expected exception not thrown" }
        } catch (e: IllegalArgumentException) {
            assert(e.message?.contains("Invalid snippetId format") == true)
        }
    }

    @Test
    fun `deleteSnippet should throw if snippet not found`() {
        val snippetId = UUID.randomUUID().toString()
        every { snippetRepository.findById(UUID.fromString(snippetId)) } returns java.util.Optional.empty()
        try {
            snippetService.deleteSnippet(snippetId, "user-123")
            assert(false) { "Expected exception not thrown" }
        } catch (e: RuntimeException) {
            assert(e.message?.contains("Snippet not found") == true)
        }
    }

    @Test
    fun `getSnippetsByFilter should filter by name`() {
        // Ambos snippets tienen ownerId = "user-123" y lintConfigService.getAllRulesFromUser retorna emptyList()
        val jwt = mockk<Jwt> { every { subject } returns "user-123" }
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val languageKotlin = mockk<LanguageVersion>(relaxed = true) { every { language.name } returns "Kotlin" }
        val languagePs = mockk<LanguageVersion>(relaxed = true) { every { language.name } returns "Printscript" }
        val snippet1 = Snippet(id1, "user-123", "Hello World", "desc1", OffsetDateTime.now(), OffsetDateTime.now(), null, languageKotlin)
        val snippet2 = Snippet(id2, "user-123", "Bye World", "desc2", OffsetDateTime.now(), OffsetDateTime.now(), null, languagePs)
        every { authApiClient.getSnippetsByAccessType("user-123", any()) } returns listOf(id1, id2)
        every { snippetRepository.findAllById(any()) } returns listOf(snippet1, snippet2)
        every { lintConfigService.getAllRulesFromUser("user-123") } returns emptyList()
        every { lintResultService.snippetPassesLinting(any()) } returns true
        // Filtro por nombre: solo snippet1 tiene el título 'Hello World'
        val page =
            snippetService.getSnippetsByFilter(
                jwt, 0, 10,
                AccessType.OWNER, "Hello World", "", null, SortByType.NAME, DirectionType.ASC,
            )
        assertEquals(1, page.content.size)
        assertEquals("Hello World", page.content[0].title)
        assertEquals("Kotlin", page.content[0].languageVersion.language.name)

        // Filtro por lenguaje: solo snippet2 tiene el lenguaje 'Printscript'
        val pageLang =
            snippetService.getSnippetsByFilter(
                jwt, 0, 10, AccessType.OWNER,
                "", "Printscript", null, SortByType.LANGUAGE, DirectionType.ASC,
            )
        assertEquals(1, pageLang.content.size)
        assertEquals("Printscript", pageLang.content[0].languageVersion.language.name)
        assertEquals("Bye World", pageLang.content[0].title)
    }

    @Test
    fun `getSnippetsByFilter should filter by language`() {
        val jwt = mockk<Jwt> { every { subject } returns "user-123" }
        val langKotlin = mockk<LanguageVersion>(relaxed = true) { every { language.name } returns "Kotlin" }
        val langPs = mockk<LanguageVersion>(relaxed = true) { every { language.name } returns "Printscript" }
        val snippet1 =
            Snippet(
                UUID.randomUUID(),
                "user-123",
                "A",
                "desc1",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                null,
                langKotlin,
            )
        val snippet2 =
            Snippet(
                UUID.randomUUID(),
                "user-123",
                "B",
                "desc2",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                null,
                langPs,
            )
        every { authApiClient.getSnippetsByAccessType("user-123", any()) } returns listOf(snippet1.id!!, snippet2.id!!)
        every { snippetRepository.findAllById(any()) } returns listOf(snippet1, snippet2)
        every { lintConfigService.getAllRulesFromUser("user-123") } returns emptyList()
        every { lintResultService.snippetPassesLinting(any()) } returns true
        // Filtro por lenguaje: solo snippet2 tiene el lenguaje 'Printscript'
        val pageLang =
            snippetService.getSnippetsByFilter(
                jwt, 0, 10,
                AccessType.OWNER, "", "Printscript", null, SortByType.LANGUAGE, DirectionType.ASC,
            )
        assertEquals(1, pageLang.content.size)
        assertEquals("Printscript", pageLang.content[0].languageVersion.language.name)
    }

    @Test
    fun `getSnippetsByFilter should filter by passedLint true`() {
        val jwt = mockk<Jwt> { every { subject } returns "user-123" }
        // Filtro por passedLint true: solo snippet1 debe pasar
        val snippet1Id = UUID.randomUUID()
        val snippet2Id = UUID.randomUUID()
        val language = mockk<LanguageVersion>(relaxed = true) { every { language.name } returns "Kotlin" }
        val snippet1 =
            Snippet(
                snippet1Id,
                "user-123",
                "A",
                "desc1",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                null,
                language,
            )
        val snippet2 =
            Snippet(
                snippet2Id,
                "user-123",
                "B",
                "desc2",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                null,
                language,
            )
        val lintConfig = mockk<LintConfig>()
        every { authApiClient.getSnippetsByAccessType("user-123", any()) } returns listOf(snippet1Id, snippet2Id)
        every { snippetRepository.findAllById(any()) } returns listOf(snippet1, snippet2)
        every { lintConfigService.getAllRulesFromUser("user-123") } returns listOf(lintConfig)
        every { lintResultService.snippetPassesLinting(snippet1Id.toString()) } returns true
        every { lintResultService.snippetPassesLinting(snippet2Id.toString()) } returns false
        val pagePassed =
            snippetService.getSnippetsByFilter(
                jwt, 0, 10, AccessType.OWNER,
                "", "", true, SortByType.NAME, DirectionType.ASC,
            )
        assertEquals(1, pagePassed.content.size)
        assertEquals(snippet1Id.toString(), pagePassed.content[0].id)
        // Filtro por passedLint false: solo snippet2 debe pasar
        val pageNotPassed =
            snippetService.getSnippetsByFilter(
                jwt, 0, 10, AccessType.OWNER,
                "", "", false, SortByType.NAME, DirectionType.ASC,
            )
        assertEquals(1, pageNotPassed.content.size)
        assertEquals(snippet2Id.toString(), pageNotPassed.content[0].id)
        // Test de paginación y orden DESC por nombre
        val snippetA =
            Snippet(
                UUID.randomUUID(),
                "user-123",
                "A",
                "desc1",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                null,
                language,
            )
        val snippetB =
            Snippet(
                UUID.randomUUID(),
                "user-123",
                "B",
                "desc2",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                null,
                language,
            )
        val snippetC =
            Snippet(
                UUID.randomUUID(),
                "user-123",
                "C",
                "desc3",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                null,
                language,
            )
        every {
            authApiClient.getSnippetsByAccessType(
                "user-123", any(),
            )
        } returns listOf(snippetA.id!!, snippetB.id!!, snippetC.id!!)
        every { snippetRepository.findAllById(any()) } returns listOf(snippetA, snippetB, snippetC)
        every {
            lintConfigService.getAllRulesFromUser(
                "user-123",
            )
        } returns emptyList()
        every { lintResultService.snippetPassesLinting(any()) } returns true
        val pagePaginated =
            snippetService.getSnippetsByFilter(
                jwt, 0, 2,
                AccessType.OWNER, "", "", null, SortByType.NAME, DirectionType.DESC,
            )
        assertEquals(2, pagePaginated.content.size)
        assertEquals("C", pagePaginated.content[0].title)
        assertEquals("B", pagePaginated.content[1].title)
        assertEquals(3, pagePaginated.totalElements)
    }
}
