package org.gudelker.snippet.service.modules.snippets

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.gudelker.snippet.service.api.AssetApiClient
import org.gudelker.snippet.service.api.AuthApiClient
import org.gudelker.snippet.service.api.EngineApiClient
import org.gudelker.snippet.service.modules.langver.LanguageVersionRepository
import org.gudelker.snippet.service.modules.linting.LintingOrchestratorService
import org.gudelker.snippet.service.modules.linting.lintconfig.LintConfigService
import org.gudelker.snippet.service.modules.linting.lintresult.LintResultService
import org.gudelker.snippet.service.modules.snippets.input.create.CreateSnippetFromEditor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt
import java.util.UUID
import kotlin.test.assertEquals

class SnippetServiceTest {
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
        snippetService = SnippetService(
            snippetRepository,
            authApiClient,
            assetApiClient,
            engineApiClient,
            lintConfigService,
            lintResultService,
            languageVersionRepository,
            orchestratorLintingService
        )
    }

    @Test
    fun `createSnippetFromEditor should save and return snippet`() {
        val input = CreateSnippetFromEditor(
            title = "Test Title",
            description = "Test Description",
            language = "kotlin",
            content = "fun main() {}",
            version = "1.0"
        )
        val jwt = mockk<Jwt> {
            every { subject } returns "user-123"
        }
        val languageVersion = mockk<org.gudelker.snippet.service.modules.langver.LanguageVersion>(relaxed = true)
        val snippet = Snippet(
            id = UUID.randomUUID(),
            ownerId = "user-123",
            title = input.title,
            description = input.description,
            languageVersion = languageVersion
        )
        every { snippetRepository.save(any()) } returns snippet
        every { languageVersionRepository.findByLanguageNameAndVersion(any(), any()) } returns languageVersion
        // Puedes mockear más dependencias según la lógica interna

        // Ejecutar
        val result = snippetService.createSnippetFromEditor(input, jwt)

        // Verificar
        assertEquals(snippet, result)
        verify { snippetRepository.save(any()) }
        verify { authApiClient.authorizeSnippet(any(), any()) }
        verify { assetApiClient.createAsset(any(), any(), any()) }
        verify { orchestratorLintingService.lintSingleSnippet(any(), any()) }
    }

    @Test
    fun `createSnippetFromEditor should throw when parseAndValidateSnippet fails`() {
        val input = CreateSnippetFromEditor(
            title = "Test Title",
            description = "Test Description",
            language = "kotlin",
            content = "fun main() {}",
            version = "1.0"
        )
        val jwt = mockk<Jwt> {
            every { subject } returns "user-123"
        }
        // Forzar excepción en parseAndValidateSnippet
        val service = spyk(snippetService) {
            every { this@spyk.parseAndValidateSnippet(any()) } throws IllegalArgumentException("Parse error")
        }
        try {
            service.createSnippetFromEditor(input, jwt)
            assert(false) { "Expected exception not thrown" }
        } catch (e: IllegalArgumentException) {
            assertEquals("Parse error", e.message)
        }
    }

    @Test
    fun `createSnippetFromEditor should throw when authorization fails`() {
        val input = CreateSnippetFromEditor(
            title = "Test Title",
            description = "Test Description",
            language = "kotlin",
            content = "fun main() {}",
            version = "1.0"
        )
        val jwt = mockk<Jwt> {
            every { subject } returns "user-123"
        }
        val languageVersion = mockk<org.gudelker.snippet.service.modules.langver.LanguageVersion>(relaxed = true)
        val snippet = Snippet(
            id = UUID.randomUUID(),
            ownerId = "user-123",
            title = input.title,
            description = input.description,
            languageVersion = languageVersion
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
        val input = CreateSnippetFromEditor(
            title = "Test Title",
            description = "Test Description",
            language = "kotlin",
            content = "fun main() {}",
            version = "1.0"
        )
        val jwt = mockk<Jwt> {
            every { subject } returns "user-123"
        }
        val languageVersion = mockk<org.gudelker.snippet.service.modules.langver.LanguageVersion>(relaxed = true)
        val snippet = Snippet(
            id = UUID.randomUUID(),
            ownerId = "user-123",
            title = input.title,
            description = input.description,
            languageVersion = languageVersion
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
}
