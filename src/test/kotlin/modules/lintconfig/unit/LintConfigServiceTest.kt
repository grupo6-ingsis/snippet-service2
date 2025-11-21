package modules.lintconfig.unit

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gudelker.snippet.service.modules.linting.lintconfig.LintConfig
import org.gudelker.snippet.service.modules.linting.lintconfig.LintConfigRepository
import org.gudelker.snippet.service.modules.linting.lintconfig.LintConfigService
import org.gudelker.snippet.service.modules.linting.lintconfig.input.ActivateRuleRequest
import org.gudelker.snippet.service.modules.linting.lintrule.LintRule
import org.gudelker.snippet.service.modules.linting.lintrule.LintRuleRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.web.server.ResponseStatusException
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class LintConfigServiceTest {
    private lateinit var lintConfigRepository: LintConfigRepository
    private lateinit var lintRuleRepository: LintRuleRepository
    private lateinit var lintConfigService: LintConfigService

    @BeforeEach
    fun setUp() {
        lintConfigRepository = mockk(relaxed = true)
        lintRuleRepository = mockk(relaxed = true)
        lintConfigService = LintConfigService(lintConfigRepository, lintRuleRepository)
    }

    @Nested
    inner class ModifyRuleTests {
        @Test
        fun `should activate new rule if not present`() {
            val ruleId = UUID.randomUUID()
            val userId = "user-1"
            val rule =
                LintRule().apply {
                    id = ruleId
                    name = "Rule1"
                    hasValue = false
                }
            val request = ActivateRuleRequest(ruleId.toString(), "Rule1", true, null, false)
            every { lintConfigRepository.findByUserIdAndLintRuleId(userId, ruleId) } returns null
            every { lintRuleRepository.findById(ruleId) } returns Optional.of(rule)
            every { lintConfigRepository.save(any()) } answers { firstArg() }
            val result = lintConfigService.modifyRule(request, userId)
            assertEquals(userId, result?.userId)
            assertEquals(rule, result?.lintRule)
            assertNull(result?.ruleValue)
        }

        @Test
        fun `should update existing active rule with value`() {
            val ruleId = UUID.randomUUID()
            val userId = "user-1"
            val rule =
                LintRule().apply {
                    id = ruleId
                    name = "Rule1"
                    hasValue = true
                }
            val config =
                LintConfig().apply {
                    this.userId = userId
                    this.lintRule = rule
                    this.ruleValue = "old"
                }
            val request = ActivateRuleRequest(ruleId.toString(), "Rule1", true, "new", true)
            every { lintConfigRepository.findByUserIdAndLintRuleId(userId, ruleId) } returns config
            every { lintConfigRepository.save(config) } returns config
            val result = lintConfigService.modifyRule(request, userId)
            assertEquals("new", result?.ruleValue)
        }

        @Test
        fun `should update existing active rule without value`() {
            val ruleId = UUID.randomUUID()
            val userId = "user-1"
            val rule =
                LintRule().apply {
                    id = ruleId
                    name = "Rule1"
                    hasValue = false
                }
            val config =
                LintConfig().apply {
                    this.userId = userId
                    this.lintRule = rule
                    this.ruleValue = "old"
                }
            val request = ActivateRuleRequest(ruleId.toString(), "Rule1", true, null, false)
            every { lintConfigRepository.findByUserIdAndLintRuleId(userId, ruleId) } returns config
            every { lintConfigRepository.save(config) } returns config
            val result = lintConfigService.modifyRule(request, userId)
            assertNull(result?.ruleValue)
        }

        @Test
        fun `should deactivate rule if exists`() {
            val ruleId = UUID.randomUUID()
            val userId = "user-1"
            val rule =
                LintRule().apply {
                    id = ruleId
                    name = "Rule1"
                }
            val config =
                LintConfig().apply {
                    this.userId = userId
                    this.lintRule = rule
                }
            val request = ActivateRuleRequest(ruleId.toString(), "Rule1", false, null, false)
            every { lintConfigRepository.findByUserIdAndLintRuleId(userId, ruleId) } returns config
            every { lintConfigRepository.delete(config) } returns Unit
            val result = lintConfigService.modifyRule(request, userId)
            assertNull(result)
            verify { lintConfigRepository.delete(config) }
        }

        @Test
        fun `should do nothing if deactivating non-existing rule`() {
            val ruleId = UUID.randomUUID()
            val userId = "user-1"
            val request = ActivateRuleRequest(ruleId.toString(), "Rule1", false, null, false)
            every { lintConfigRepository.findByUserIdAndLintRuleId(userId, ruleId) } returns null
            val result = lintConfigService.modifyRule(request, userId)
            assertNull(result)
        }

        @Test
        fun `should throw if activating rule with value but value is null`() {
            val ruleId = UUID.randomUUID()
            val userId = "user-1"
            val rule =
                LintRule().apply {
                    id = ruleId
                    name = "Rule1"
                    hasValue = true
                }
            val request = ActivateRuleRequest(ruleId.toString(), "Rule1", true, null, true)
            every { lintConfigRepository.findByUserIdAndLintRuleId(userId, ruleId) } returns null
            every { lintRuleRepository.findById(ruleId) } returns Optional.of(rule)
            assertFailsWith<ResponseStatusException> {
                lintConfigService.modifyRule(request, userId)
            }
        }

        @Test
        fun `should throw if updating rule with value but value is null`() {
            val ruleId = UUID.randomUUID()
            val userId = "user-1"
            val rule =
                LintRule().apply {
                    id = ruleId
                    name = "Rule1"
                    hasValue = true
                }
            val config =
                LintConfig().apply {
                    this.userId = userId
                    this.lintRule = rule
                }
            val request = ActivateRuleRequest(ruleId.toString(), "Rule1", true, null, true)
            every { lintConfigRepository.findByUserIdAndLintRuleId(userId, ruleId) } returns config
            assertFailsWith<ResponseStatusException> {
                lintConfigService.modifyRule(request, userId)
            }
        }

        @Test
        fun `should throw if activating rule that does not exist`() {
            val ruleId = UUID.randomUUID()
            val userId = "user-1"
            val request = ActivateRuleRequest(ruleId.toString(), "Rule1", true, null, false)
            every { lintConfigRepository.findByUserIdAndLintRuleId(userId, ruleId) } returns null
            every { lintRuleRepository.findById(ruleId) } returns Optional.empty()
            assertFailsWith<ResponseStatusException> {
                lintConfigService.modifyRule(request, userId)
            }
        }
    }

    @Nested
    inner class GetAllRulesFromUserTests {
        @Test
        fun `should return all rules for user`() {
            val userId = "user-1"
            val config1 = LintConfig().apply { this.userId = userId }
            val config2 = LintConfig().apply { this.userId = userId }
            every { lintConfigRepository.findByUserId(userId) } returns listOf(config1, config2)
            val result = lintConfigService.getAllRulesFromUser(userId)
            assertEquals(2, result.size)
            assert(result.all { it.userId == userId })
        }

        @Test
        fun `should return empty list if user has no rules`() {
            val userId = "user-1"
            every { lintConfigRepository.findByUserId(userId) } returns emptyList()
            val result = lintConfigService.getAllRulesFromUser(userId)
            assertEquals(0, result.size)
        }
    }
}
