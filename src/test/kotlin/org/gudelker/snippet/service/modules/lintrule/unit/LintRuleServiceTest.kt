package modules.lintrule.unit

import io.mockk.every
import io.mockk.mockk
import org.gudelker.snippet.service.modules.linting.lintrule.LintRule
import org.gudelker.snippet.service.modules.linting.lintrule.LintRuleRepository
import org.gudelker.snippet.service.modules.linting.lintrule.LintRuleService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LintRuleServiceTest {
    private lateinit var lintRuleRepository: LintRuleRepository
    private lateinit var lintRuleService: LintRuleService

    @BeforeEach
    fun setUp() {
        lintRuleRepository = mockk(relaxed = true)
        lintRuleService = LintRuleService(lintRuleRepository)
    }

    @Nested
    inner class GetLintRuleByIdTests {
        @Test
        fun `should return rule when id exists`() {
            val ruleId = UUID.randomUUID()
            val rule =
                LintRule().apply {
                    id = ruleId
                    name = "Rule1"
                }
            every { lintRuleRepository.findById(ruleId) } returns Optional.of(rule)
            val result = lintRuleService.getLintRuleById(ruleId.toString())
            assertTrue(result.isPresent)
            assertEquals(rule, result.get())
        }

        @Test
        fun `should return empty when id does not exist`() {
            val ruleId = UUID.randomUUID()
            every { lintRuleRepository.findById(ruleId) } returns Optional.empty()
            val result = lintRuleService.getLintRuleById(ruleId.toString())
            assertTrue(result.isEmpty)
        }

        @Test
        fun `should throw exception for invalid UUID`() {
            try {
                lintRuleService.getLintRuleById("not-a-uuid")
                assert(false) { "Expected exception not thrown" }
            } catch (e: Exception) {
                assertTrue(e is IllegalArgumentException)
            }
        }
    }

    @Nested
    inner class GetLintRulesByNameTests {
        @Test
        fun `should return rules with matching name`() {
            val rule =
                LintRule().apply {
                    id = UUID.randomUUID()
                    name = "Rule1"
                }
            every { lintRuleRepository.findByName("Rule1") } returns listOf(rule)
            val result = lintRuleService.getLintRulesByName("Rule1")
            assertEquals(1, result.size)
            assertEquals("Rule1", result[0].name)
        }

        @Test
        fun `should return empty list if no rules match name`() {
            every { lintRuleRepository.findByName("NoMatch") } returns emptyList()
            val result = lintRuleService.getLintRulesByName("NoMatch")
            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class GetAllLintRulesTests {
        @Test
        fun `should return all rules`() {
            val rule1 =
                LintRule().apply {
                    id = UUID.randomUUID()
                    name = "Rule1"
                }
            val rule2 =
                LintRule().apply {
                    id = UUID.randomUUID()
                    name = "Rule2"
                }
            every { lintRuleRepository.findAll() } returns listOf(rule1, rule2)
            val result = lintRuleService.getAllLintRules()
            assertEquals(2, result.size)
            assertTrue(result.containsAll(listOf(rule1, rule2)))
        }

        @Test
        fun `should return empty list if no rules exist`() {
            every { lintRuleRepository.findAll() } returns emptyList()
            val result = lintRuleService.getAllLintRules()
            assertTrue(result.isEmpty())
        }
    }
}
