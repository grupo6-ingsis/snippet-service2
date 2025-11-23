package modules.lintorchestrator.unit

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gudelker.snippet.service.modules.linting.LintingOrchestratorService
import org.gudelker.snippet.service.modules.linting.lintconfig.LintConfig
import org.gudelker.snippet.service.modules.linting.lintconfig.LintConfigService
import org.gudelker.snippet.service.modules.linting.lintrule.LintRule
import org.gudelker.snippet.service.modules.linting.lintrule.LintRuleRepository
import org.gudelker.snippet.service.modules.snippets.Snippet
import org.gudelker.snippet.service.modules.snippets.SnippetRepository
import org.gudelker.snippet.service.redis.producer.LintPublisher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.web.client.HttpClientErrorException
import java.util.Optional
import java.util.UUID
import kotlin.test.assertFailsWith

class LintOrchestratorTests {
    private lateinit var snippetRepository: SnippetRepository
    private lateinit var lintConfigService: LintConfigService
    private lateinit var lintRuleRepository: LintRuleRepository
    private lateinit var lintPublisher: LintPublisher
    private lateinit var orchestrator: LintingOrchestratorService

    @BeforeEach
    fun setUp() {
        snippetRepository = mockk(relaxed = true)
        lintConfigService = mockk(relaxed = true)
        lintRuleRepository = mockk(relaxed = true)
        lintPublisher = mockk(relaxed = true)
        orchestrator =
            LintingOrchestratorService(
                snippetRepository,
                lintConfigService,
                lintRuleRepository,
                lintPublisher,
            )
    }

    @Nested
    inner class LintSingleSnippetTests {
        @Test
        fun `should publish lint request for single snippet with user rules`() {
            val snippetId = UUID.randomUUID()
            val userId = "user-1"
            val rule = LintRule().apply { name = "Rule1" }
            val config =
                mockk<LintConfig> {
                    every { lintRule } returns rule
                    every { ruleValue } returns "val"
                }
            every { lintConfigService.getAllRulesFromUser(userId) } returns listOf(config)
            val snippet =
                mockk<Snippet> {
                    every { id } returns snippetId
                    every { languageVersion.version } returns "1.0"
                }
            every { snippetRepository.findById(snippetId) } returns Optional.of(snippet)
            every { lintRuleRepository.findAll() } returns listOf(rule)
            every { lintPublisher.publishLintRequest(any()) } returns null
            orchestrator.lintSingleSnippet(snippetId, userId)
            verify { lintPublisher.publishLintRequest(any()) }
        }
    }

    @Nested
    inner class LintUserSnippetsTests {
        @Test
        fun `should publish lint requests for all user snippets`() {
            val userId = "user-1"
            val snippetId1 = UUID.randomUUID()
            val snippetId2 = UUID.randomUUID()
            val rule = LintRule().apply { name = "Rule1" }
            val config =
                mockk<LintConfig> {
                    every { lintRule } returns rule
                    every { ruleValue } returns "val"
                }
            val snippet1 =
                mockk<Snippet> {
                    every { id } returns snippetId1
                    every { languageVersion.version } returns "1.0"
                }
            val snippet2 =
                mockk<Snippet> {
                    every { id } returns snippetId2
                    every { languageVersion.version } returns "1.0"
                }
            every { snippetRepository.findByOwnerId(userId) } returns listOf(snippet1, snippet2)
            every { lintConfigService.getAllRulesFromUser(userId) } returns listOf(config)
            every { snippetRepository.findById(snippetId1) } returns Optional.of(snippet1)
            every { snippetRepository.findById(snippetId2) } returns Optional.of(snippet2)
            every { lintRuleRepository.findAll() } returns listOf(rule)
            every { lintPublisher.publishLintRequest(any()) } returns null
            orchestrator.lintUserSnippets(userId)
            verify(exactly = 2) { lintPublisher.publishLintRequest(any()) }
        }
    }

    @Nested
    inner class LintSnippetsErrorTests {
        @Test
        fun `should throw HttpClientErrorException if snippet not found`() {
            val snippetId = UUID.randomUUID()
            val userId = "user-1"
            val rule = LintRule().apply { name = "Rule1" }
            val config =
                mockk<LintConfig> {
                    every { lintRule } returns rule
                    every { ruleValue } returns "val"
                }
            every { lintConfigService.getAllRulesFromUser(userId) } returns listOf(config)
            every { lintRuleRepository.findAll() } returns listOf(rule)
            every { snippetRepository.findById(snippetId) } returns Optional.empty()
            assertFailsWith<HttpClientErrorException> {
                orchestrator.lintSingleSnippet(snippetId, userId)
            }
        }
    }
}
