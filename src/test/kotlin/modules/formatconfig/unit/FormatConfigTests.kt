package modules.formatconfig.unit

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gudelker.snippet.service.modules.formatting.formatconfig.FormatConfig
import org.gudelker.snippet.service.modules.formatting.formatconfig.FormatConfigRepository
import org.gudelker.snippet.service.modules.formatting.formatconfig.FormatConfigService
import org.gudelker.snippet.service.modules.formatting.formatconfig.input.ActivateFormatRuleRequest
import org.gudelker.snippet.service.modules.formatting.formatrule.FormatRule
import org.gudelker.snippet.service.modules.formatting.formatrule.FormatRuleRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.web.server.ResponseStatusException
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class FormatConfigTests {
    private lateinit var formatConfigRepository: FormatConfigRepository
    private lateinit var formatRuleRepository: FormatRuleRepository
    private lateinit var formatConfigService: FormatConfigService

    @BeforeEach
    fun setUp() {
        formatConfigRepository = mockk(relaxed = true)
        formatRuleRepository = mockk(relaxed = true)
        formatConfigService = FormatConfigService(formatConfigRepository, formatRuleRepository)
    }

    @Nested
    inner class ModifyFormatConfigTests {
        @Test
        fun `should activate new rule if not present`() {
            val ruleId = UUID.randomUUID()
            val userId = "user-1"
            val rule =
                FormatRule().apply {
                    id = ruleId
                    name = "Rule1"
                    hasValue = false
                }
            val request = ActivateFormatRuleRequest(ruleId.toString(), "Rule1", true, null, false)
            every { formatConfigRepository.findByUserIdAndFormatRuleId(userId, ruleId) } returns null
            every { formatRuleRepository.findById(ruleId) } returns Optional.of(rule)
            every { formatConfigRepository.save(any()) } answers { firstArg() }
            val result = formatConfigService.modifyFormatConfig(request, userId)
            assertEquals(userId, result?.userId)
            assertEquals(rule, result?.formatRule)
            assertNull(result?.ruleValue)
        }

        @Test
        fun `should update existing active rule with value`() {
            val ruleId = UUID.randomUUID()
            val userId = "user-1"
            val rule =
                FormatRule().apply {
                    id = ruleId
                    name = "Rule1"
                    hasValue = true
                }
            val config =
                FormatConfig().apply {
                    this.userId = userId
                    this.formatRule = rule
                    this.ruleValue = 1
                }
            val request = ActivateFormatRuleRequest(ruleId.toString(), "Rule1", true, "2", true)
            every { formatConfigRepository.findByUserIdAndFormatRuleId(userId, ruleId) } returns config
            every { formatConfigRepository.save(config) } returns config
            val result = formatConfigService.modifyFormatConfig(request, userId)
            assertEquals(2, result?.ruleValue)
        }

        @Test
        fun `should update existing active rule without value`() {
            val ruleId = UUID.randomUUID()
            val userId = "user-1"
            val rule =
                FormatRule().apply {
                    id = ruleId
                    name = "Rule1"
                    hasValue = false
                }
            val config =
                FormatConfig().apply {
                    this.userId = userId
                    this.formatRule = rule
                    this.ruleValue = 1
                }
            val request = ActivateFormatRuleRequest(ruleId.toString(), "Rule1", true, null, false)
            every { formatConfigRepository.findByUserIdAndFormatRuleId(userId, ruleId) } returns config
            every { formatConfigRepository.save(config) } returns config
            val result = formatConfigService.modifyFormatConfig(request, userId)
            assertNull(result?.ruleValue)
        }

        @Test
        fun `should deactivate rule if exists`() {
            val ruleId = UUID.randomUUID()
            val userId = "user-1"
            val rule =
                FormatRule().apply {
                    id = ruleId
                    name = "Rule1"
                }
            val config =
                FormatConfig().apply {
                    this.userId = userId
                    this.formatRule = rule
                }
            val request = ActivateFormatRuleRequest(ruleId.toString(), "Rule1", false, null, false)
            every { formatConfigRepository.findByUserIdAndFormatRuleId(userId, ruleId) } returns config
            every { formatConfigRepository.delete(config) } returns Unit
            val result = formatConfigService.modifyFormatConfig(request, userId)
            assertNull(result)
            verify { formatConfigRepository.delete(config) }
        }

        @Test
        fun `should do nothing if deactivating non-existing rule`() {
            val ruleId = UUID.randomUUID()
            val userId = "user-1"
            val request = ActivateFormatRuleRequest(ruleId.toString(), "Rule1", false, null, false)
            every { formatConfigRepository.findByUserIdAndFormatRuleId(userId, ruleId) } returns null
            val result = formatConfigService.modifyFormatConfig(request, userId)
            assertNull(result)
        }

        @Test
        fun `should throw if activating rule with value but value is null`() {
            val ruleId = UUID.randomUUID()
            val userId = "user-1"
            val rule =
                FormatRule().apply {
                    id = ruleId
                    name = "Rule1"
                    hasValue = true
                }
            val request = ActivateFormatRuleRequest(ruleId.toString(), "Rule1", true, null, true)
            every { formatConfigRepository.findByUserIdAndFormatRuleId(userId, ruleId) } returns null
            every { formatRuleRepository.findById(ruleId) } returns Optional.of(rule)
            assertFailsWith<ResponseStatusException> {
                formatConfigService.modifyFormatConfig(request, userId)
            }
        }

        @Test
        fun `should throw if updating rule with value but value is null`() {
            val ruleId = UUID.randomUUID()
            val userId = "user-1"
            val rule =
                FormatRule().apply {
                    id = ruleId
                    name = "Rule1"
                    hasValue = true
                }
            val config =
                FormatConfig().apply {
                    this.userId = userId
                    this.formatRule = rule
                }
            val request = ActivateFormatRuleRequest(ruleId.toString(), "Rule1", true, null, true)
            every { formatConfigRepository.findByUserIdAndFormatRuleId(userId, ruleId) } returns config
            assertFailsWith<ResponseStatusException> {
                formatConfigService.modifyFormatConfig(request, userId)
            }
        }

        @Test
        fun `should throw if activating rule that does not exist`() {
            val ruleId = UUID.randomUUID()
            val userId = "user-1"
            val request = ActivateFormatRuleRequest(ruleId.toString(), "Rule1", true, null, false)
            every { formatConfigRepository.findByUserIdAndFormatRuleId(userId, ruleId) } returns null
            every { formatRuleRepository.findById(ruleId) } returns Optional.empty()
            assertFailsWith<ResponseStatusException> {
                formatConfigService.modifyFormatConfig(request, userId)
            }
        }
    }

    @Nested
    inner class GetAllRulesFromUserTests {
        @Test
        fun `should return all rules for user`() {
            val userId = "user-1"
            val config1 = FormatConfig().apply { this.userId = userId }
            val config2 = FormatConfig().apply { this.userId = userId }
            every { formatConfigRepository.findByUserId(userId) } returns listOf(config1, config2)
            val result = formatConfigService.getAllRulesFromUser(userId)
            assertEquals(2, result.size)
            assert(result.all { it.userId == userId })
        }

        @Test
        fun `should return empty list if user has no rules`() {
            val userId = "user-1"
            every { formatConfigRepository.findByUserId(userId) } returns emptyList()
            val result = formatConfigService.getAllRulesFromUser(userId)
            assertEquals(0, result.size)
        }
    }
}
