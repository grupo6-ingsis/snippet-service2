package modules.formatorchestrator.unit

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gudelker.snippet.service.modules.formatting.FormattingOrchestratorService
import org.gudelker.snippet.service.modules.formatting.formatconfig.FormatConfig
import org.gudelker.snippet.service.modules.formatting.formatconfig.FormatConfigService
import org.gudelker.snippet.service.modules.formatting.formatrule.FormatRule
import org.gudelker.snippet.service.modules.formatting.formatrule.FormatRuleRepository
import org.gudelker.snippet.service.modules.snippets.Snippet
import org.gudelker.snippet.service.modules.snippets.SnippetRepository
import org.gudelker.snippet.service.redis.producer.FormatPublisher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.web.client.HttpClientErrorException
import java.util.Optional
import java.util.UUID
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FormatOrchestratorTests {
    private lateinit var snippetRepository: SnippetRepository
    private lateinit var formatConfigService: FormatConfigService
    private lateinit var formatRuleRepository: FormatRuleRepository
    private lateinit var formatPublisher: FormatPublisher
    private lateinit var orchestrator: FormattingOrchestratorService

    @BeforeEach
    fun setUp() {
        snippetRepository = mockk(relaxed = true)
        formatConfigService = mockk(relaxed = true)
        formatRuleRepository = mockk(relaxed = true)
        formatPublisher = mockk(relaxed = true)
        orchestrator =
            FormattingOrchestratorService(
                snippetRepository,
                formatConfigService,
                formatRuleRepository,
                formatPublisher,
            )
    }

    @Nested
    inner class FormatSingleSnippetTests {
        @Test
        fun `should publish format request for single snippet with user rules`() {
            val snippetId = UUID.randomUUID()
            val userId = "user-1"
            val rule = FormatRule().apply { name = "Rule1" }
            val config =
                FormatConfig().apply {
                    this.formatRule = rule
                    this.ruleValue = 2
                    this.userId = userId
                }
            val snippet =
                mockk<Snippet> {
                    every { id } returns snippetId
                    every { languageVersion.version } returns "1.0"
                }
            every { formatConfigService.getAllRulesFromUser(userId) } returns listOf(config)
            every { snippetRepository.findById(snippetId) } returns Optional.of(snippet)
            every { formatRuleRepository.findAll() } returns listOf(rule)
            every { formatPublisher.publishFormatRequest(any()) } returns null
            val result = orchestrator.formatSingleSnippet(snippetId, userId)
            assertTrue(result.contains("Formatting request published"))
            verify { formatPublisher.publishFormatRequest(any()) }
        }

        @Test
        fun `should throw HttpClientErrorException if snippet not found`() {
            val snippetId = UUID.randomUUID()
            val userId = "user-1"
            val rule = FormatRule().apply { name = "Rule1" }
            val config =
                FormatConfig().apply {
                    this.formatRule = rule
                    this.ruleValue = 2
                    this.userId = userId
                }
            every { formatConfigService.getAllRulesFromUser(userId) } returns listOf(config)
            every { snippetRepository.findById(snippetId) } returns Optional.empty()
            every { formatRuleRepository.findAll() } returns listOf(rule)
            every { formatPublisher.publishFormatRequest(any()) } returns null
            assertFailsWith<HttpClientErrorException> {
                orchestrator.formatSingleSnippet(snippetId, userId)
            }
        }
    }

    @Nested
    inner class FormatUserSnippetsTests {
        @Test
        fun `should publish format requests for all user snippets`() {
            val userId = "user-1"
            val snippetId1 = UUID.randomUUID()
            val snippetId2 = UUID.randomUUID()
            val rule = FormatRule().apply { name = "Rule1" }
            val config =
                FormatConfig().apply {
                    this.formatRule = rule
                    this.ruleValue = 2
                    this.userId = userId
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
            every { formatConfigService.getAllRulesFromUser(userId) } returns listOf(config)
            every { snippetRepository.findById(snippetId1) } returns Optional.of(snippet1)
            every { snippetRepository.findById(snippetId2) } returns Optional.of(snippet2)
            every { formatRuleRepository.findAll() } returns listOf(rule)
            every { formatPublisher.publishFormatRequest(any()) } returns null
            orchestrator.formatUserSnippets(userId)
            verify(exactly = 2) { formatPublisher.publishFormatRequest(any()) }
        }

        @Test
        fun `should throw HttpClientErrorException if any snippet not found`() {
            val userId = "user-1"
            val snippetId1 = UUID.randomUUID()
            val snippetId2 = UUID.randomUUID()
            val rule = FormatRule().apply { name = "Rule1" }
            val config =
                FormatConfig().apply {
                    this.formatRule = rule
                    this.ruleValue = 2
                    this.userId = userId
                }
            val snippet1 =
                mockk<Snippet> {
                    every { id } returns snippetId1
                    every { languageVersion.version } returns "1.0"
                }
            every { snippetRepository.findByOwnerId(userId) } returns listOf(snippet1)
            every { formatConfigService.getAllRulesFromUser(userId) } returns listOf(config)
            every { snippetRepository.findById(snippetId1) } returns Optional.empty()
            every { formatRuleRepository.findAll() } returns listOf(rule)
            every { formatPublisher.publishFormatRequest(any()) } returns null
            assertFailsWith<HttpClientErrorException> {
                orchestrator.formatUserSnippets(userId)
            }
        }
    }
}
