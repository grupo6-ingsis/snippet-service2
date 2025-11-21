package modules.lintresult.unit

import io.mockk.every
import io.mockk.mockk
import org.gudelker.snippet.service.modules.linting.lintresult.LintError
import org.gudelker.snippet.service.modules.linting.lintresult.LintResult
import org.gudelker.snippet.service.modules.linting.lintresult.LintResultRepository
import org.gudelker.snippet.service.modules.linting.lintresult.LintResultService
import org.gudelker.snippet.service.modules.snippets.Snippet
import org.gudelker.snippet.service.modules.snippets.SnippetRepository
import org.gudelker.snippet.service.modules.snippets.dto.types.ComplianceType
import org.gudelker.snippet.service.redis.dto.LintResultRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LintResultServiceTests {
    private lateinit var lintResultRepository: LintResultRepository
    private lateinit var snippetRepository: SnippetRepository
    private lateinit var lintResultService: LintResultService

    @BeforeEach
    fun setUp() {
        lintResultRepository = mockk(relaxed = true)
        snippetRepository = mockk(relaxed = true)
        lintResultService = LintResultService(lintResultRepository, snippetRepository)
    }

    @Nested
    inner class CreateOrUpdateLintResultTests {
        @Test
        fun `should create new lint result as compliant if no errors`() {
            val snippetId = UUID.randomUUID().toString()
            val snippet = mockk<Snippet> { every { id } returns UUID.fromString(snippetId) }
            every { snippetRepository.findById(UUID.fromString(snippetId)) } returns Optional.of(snippet)
            every { lintResultRepository.findBySnippetId(UUID.fromString(snippetId)) } returns null
            every { lintResultRepository.save(any()) } answers { firstArg() }
            val result = lintResultService.createOrUpdateLintResult(snippetId, emptyList())
            assertEquals(snippetId, result.snippetId)
            assertTrue(result.results.isEmpty())
        }

        @Test
        fun `should create new lint result as non-compliant if errors`() {
            val snippetId = UUID.randomUUID().toString()
            val snippet = mockk<Snippet> { every { id } returns UUID.fromString(snippetId) }
            val errors = listOf(LintResultRequest("msg", 1, 2))
            every { snippetRepository.findById(UUID.fromString(snippetId)) } returns Optional.of(snippet)
            every { lintResultRepository.findBySnippetId(UUID.fromString(snippetId)) } returns null
            every { lintResultRepository.save(any()) } answers { firstArg() }
            val result = lintResultService.createOrUpdateLintResult(snippetId, errors)
            assertEquals(snippetId, result.snippetId)
            assertEquals(1, result.results.size)
            assertEquals("msg", result.results[0].message)
        }

        @Test
        fun `should update existing lint result`() {
            val snippetId = UUID.randomUUID().toString()
            val snippet = mockk<Snippet> { every { id } returns UUID.fromString(snippetId) }
            val existing =
                LintResult().apply {
                    this.snippet = snippet
                    this.complianceType = ComplianceType.PENDING
                }
            val errors = listOf(LintResultRequest("err", 2, 3))
            every { snippetRepository.findById(UUID.fromString(snippetId)) } returns Optional.of(snippet)
            every { lintResultRepository.findBySnippetId(UUID.fromString(snippetId)) } returns existing
            every { lintResultRepository.save(existing) } returns existing
            val result = lintResultService.createOrUpdateLintResult(snippetId, errors)
            assertEquals(snippetId, result.snippetId)
            assertEquals(1, result.results.size)
            assertEquals("err", result.results[0].message)
        }

        @Test
        fun `should throw if snippet not found`() {
            val snippetId = UUID.randomUUID().toString()
            every { snippetRepository.findById(UUID.fromString(snippetId)) } returns Optional.empty()
            assertFailsWith<IllegalArgumentException> {
                lintResultService.createOrUpdateLintResult(snippetId, emptyList())
            }
        }
    }

    @Nested
    inner class GetLintResultBySnippetIdTests {
        @Test
        fun `should return lint result if exists`() {
            val snippetId = UUID.randomUUID()
            val lintResult =
                LintResult().apply {
                    this.snippet = mockk()
                    this.complianceType = ComplianceType.COMPLIANT
                }
            every { lintResultRepository.findBySnippetId(snippetId) } returns lintResult
            val result = lintResultService.getLintResultBySnippetId(snippetId)
            assertEquals(lintResult, result)
        }

        @Test
        fun `should return null if not exists`() {
            val snippetId = UUID.randomUUID()
            every { lintResultRepository.findBySnippetId(snippetId) } returns null
            val result = lintResultService.getLintResultBySnippetId(snippetId)
            assertNull(result)
        }
    }

    @Nested
    inner class GetSnippetLintComplianceTypeTests {
        @Test
        fun `should return compliance type if exists`() {
            val snippetId = UUID.randomUUID()
            val lintResult = LintResult().apply { this.complianceType = ComplianceType.NON_COMPLIANT }
            every { lintResultRepository.findBySnippetId(snippetId) } returns lintResult
            val result = lintResultService.getSnippetLintComplianceType(snippetId)
            assertEquals(ComplianceType.NON_COMPLIANT, result)
        }

        @Test
        fun `should return null if not exists`() {
            val snippetId = UUID.randomUUID()
            every { lintResultRepository.findBySnippetId(snippetId) } returns null
            val result = lintResultService.getSnippetLintComplianceType(snippetId)
            assertNull(result)
        }
    }

    @Nested
    inner class GetSnippetLintErrorsTests {
        @Test
        fun `should return errors if present`() {
            val snippetId = UUID.randomUUID()
            val lintResult = LintResult().apply { errors = mutableListOf(LintError("msg", 1, 2)) }
            every { lintResultRepository.findBySnippetId(snippetId) } returns lintResult
            val result = lintResultService.getSnippetLintErrors(snippetId)
            assertEquals(1, result.size)
            assertEquals("msg", result[0].message)
        }

        @Test
        fun `should return empty list if no result`() {
            val snippetId = UUID.randomUUID()
            every { lintResultRepository.findBySnippetId(snippetId) } returns null
            val result = lintResultService.getSnippetLintErrors(snippetId)
            assertTrue(result.isEmpty())
        }
    }

    @Nested
    inner class SnippetPassesLintingTests {
        @Test
        fun `should return true if compliant`() {
            val snippetId = UUID.randomUUID()
            val lintResult = LintResult().apply { complianceType = ComplianceType.COMPLIANT }
            every { lintResultRepository.findBySnippetId(snippetId) } returns lintResult
            val result = lintResultService.snippetPassesLinting(snippetId.toString())
            assertTrue(result)
        }

        @Test
        fun `should return true if pending`() {
            val snippetId = UUID.randomUUID()
            val lintResult = LintResult().apply { complianceType = ComplianceType.PENDING }
            every { lintResultRepository.findBySnippetId(snippetId) } returns lintResult
            val result = lintResultService.snippetPassesLinting(snippetId.toString())
            assertTrue(result)
        }

        @Test
        fun `should return false if non-compliant`() {
            val snippetId = UUID.randomUUID()
            val lintResult = LintResult().apply { complianceType = ComplianceType.NON_COMPLIANT }
            every { lintResultRepository.findBySnippetId(snippetId) } returns lintResult
            val result = lintResultService.snippetPassesLinting(snippetId.toString())
            assertFalse(result)
        }

        @Test
        fun `should return false if no result`() {
            val snippetId = UUID.randomUUID()
            every { lintResultRepository.findBySnippetId(snippetId) } returns null
            val result = lintResultService.snippetPassesLinting(snippetId.toString())
            assertFalse(result)
        }
    }
}
