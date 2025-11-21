package modules.testsnippet.unit

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gudelker.snippet.service.api.AssetApiClient
import org.gudelker.snippet.service.api.AuthApiClient
import org.gudelker.snippet.service.api.EngineApiClient
import org.gudelker.snippet.service.modules.snippets.Snippet
import org.gudelker.snippet.service.modules.snippets.SnippetRepository
import org.gudelker.snippet.service.modules.testsnippet.TestSnippet
import org.gudelker.snippet.service.modules.testsnippet.TestSnippetRepository
import org.gudelker.snippet.service.modules.testsnippet.TestSnippetService
import org.gudelker.snippet.service.modules.testsnippet.input.CreateTestSnippetRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestSnippetServiceTests {
    private lateinit var testSnippetRepository: TestSnippetRepository
    private lateinit var snippetRepository: SnippetRepository
    private lateinit var authApiClient: AuthApiClient
    private lateinit var engineApiClient: EngineApiClient
    private lateinit var assetApiClient: AssetApiClient
    private lateinit var testSnippetService: TestSnippetService

    @BeforeEach
    fun setUp() {
        testSnippetRepository = mockk(relaxed = true)
        snippetRepository = mockk(relaxed = true)
        authApiClient = mockk(relaxed = true)
        engineApiClient = mockk(relaxed = true)
        assetApiClient = mockk(relaxed = true)
        testSnippetService =
            TestSnippetService(
                testSnippetRepository,
                snippetRepository,
                authApiClient,
                engineApiClient,
                assetApiClient,
            )
    }

    @Nested
    inner class CreateTestSnippetTests {
        @Test
        fun `should create test snippet successfully`() {
            val snippetId = UUID.randomUUID()
            val userId = "user-1"
            val snippet = mockk<Snippet>(relaxed = true)
            val request = CreateTestSnippetRequest("", snippetId.toString(), "test", mutableListOf("1"), mutableListOf("2"))
            every { snippetRepository.findById(snippetId) } returns Optional.of(snippet)
            every { authApiClient.isUserAuthorizedToWriteSnippet(snippetId.toString(), userId) } returns true
            val testSnippet =
                TestSnippet().apply {
                    name = request.name
                    input = request.input
                    expectedOutput = request.expectedOutput
                    this.snippet = snippet
                }
            every { testSnippetRepository.save(any()) } returns testSnippet
            val result = testSnippetService.createTestSnippet(request, userId)
            assertEquals(request.name, result.name)
            assertEquals(request.input, result.input)
            assertEquals(request.expectedOutput, result.expectedOutput)
            assertEquals(snippet, result.snippet)
        }

        @Test
        fun `should throw if snippet not found`() {
            val snippetId = UUID.randomUUID()
            val userId = "user-1"
            val request = CreateTestSnippetRequest("", snippetId.toString(), "test", mutableListOf("1"), mutableListOf("2"))
            every { snippetRepository.findById(snippetId) } returns Optional.empty()
            assertFailsWith<IllegalArgumentException> {
                testSnippetService.createTestSnippet(request, userId)
            }
        }

        @Test
        fun `should throw if not authorized`() {
            val snippetId = UUID.randomUUID()
            val userId = "user-1"
            val snippet = mockk<Snippet>(relaxed = true)
            val request = CreateTestSnippetRequest("", snippetId.toString(), "test", mutableListOf("1"), mutableListOf("2"))
            every { snippetRepository.findById(snippetId) } returns Optional.of(snippet)
            every { authApiClient.isUserAuthorizedToWriteSnippet(snippetId.toString(), userId) } returns false
            assertFailsWith<IllegalAccessException> {
                testSnippetService.createTestSnippet(request, userId)
            }
        }
    }

    @Nested
    inner class DeleteTestSnippetTests {
        @Test
        fun `should delete test snippet by id`() {
            val id = UUID.randomUUID()
            every { testSnippetRepository.deleteById(id) } returns Unit
            testSnippetService.deleteTestSnippet(id)
            verify { testSnippetRepository.deleteById(id) }
        }
    }

    @Nested
    inner class GetTestSnippetsBySnippetIdTests {
        @Test
        fun `should return test snippets for snippet`() {
            val snippetId = UUID.randomUUID()
            val snippet = mockk<Snippet>(relaxed = true) { every { id } returns snippetId }
            val testSnippet =
                TestSnippet().apply {
                    name = "test"
                    input = mutableListOf("1")
                    expectedOutput = mutableListOf("2")
                    this.snippet = snippet
                }
            every { snippetRepository.findById(snippetId) } returns Optional.of(snippet)
            every { testSnippetRepository.findAllBySnippetId(snippetId) } returns listOf(testSnippet)
            val result = testSnippetService.getTestSnippetsBySnippetId(snippetId)
            assertEquals(1, result.size)
            assertEquals("test", result[0].name)
            assertEquals(listOf("1"), result[0].input)
            assertEquals(listOf("2"), result[0].output)
            assertEquals(snippetId.toString(), result[0].snippetId)
        }

        @Test
        fun `should throw if snippet not found`() {
            val snippetId = UUID.randomUUID()
            every { snippetRepository.findById(snippetId) } returns Optional.empty()
            assertFailsWith<IllegalArgumentException> {
                testSnippetService.getTestSnippetsBySnippetId(snippetId)
            }
        }
    }

    @Nested
    inner class UpdateTestSnippetTests {
        @Test
        fun `should update test snippet successfully`() {
            val id = UUID.randomUUID().toString()
            val snippetId = UUID.randomUUID().toString()
            val userId = "user-1"
            val snippet = mockk<Snippet>(relaxed = true)
            val testSnippet =
                TestSnippet().apply {
                    name = "old"
                    input = mutableListOf("a")
                    expectedOutput = mutableListOf("b")
                    this.snippet = snippet
                }
            val request = CreateTestSnippetRequest(id, snippetId, "new", mutableListOf("x"), mutableListOf("y"))
            every { testSnippetRepository.findById(UUID.fromString(id)) } returns Optional.of(testSnippet)
            every { snippetRepository.findById(UUID.fromString(snippetId)) } returns Optional.of(snippet)
            every { authApiClient.isUserAuthorizedToWriteSnippet(snippetId, userId) } returns true
            every { testSnippetRepository.save(testSnippet) } returns testSnippet
            val result = testSnippetService.updateTestSnippet(id, request, userId)
            assertEquals("new", result.name)
            assertEquals(mutableListOf("x"), result.input)
            assertEquals(mutableListOf("y"), result.expectedOutput)
            assertEquals(snippet, result.snippet)
        }

        @Test
        fun `should throw if test snippet not found`() {
            val id = UUID.randomUUID().toString()
            val userId = "user-1"
            val request = CreateTestSnippetRequest(id, UUID.randomUUID().toString(), "new", mutableListOf("x"), mutableListOf("y"))
            every { testSnippetRepository.findById(UUID.fromString(id)) } returns Optional.empty()
            assertFailsWith<IllegalArgumentException> {
                testSnippetService.updateTestSnippet(id, request, userId)
            }
        }

        @Test
        fun `should throw if snippet not found`() {
            val id = UUID.randomUUID().toString()
            val snippetId = UUID.randomUUID().toString()
            val userId = "user-1"
            val testSnippet = TestSnippet()
            val request = CreateTestSnippetRequest(id, snippetId, "new", mutableListOf("x"), mutableListOf("y"))
            every { testSnippetRepository.findById(UUID.fromString(id)) } returns Optional.of(testSnippet)
            every { snippetRepository.findById(UUID.fromString(snippetId)) } returns Optional.empty()
            assertFailsWith<IllegalArgumentException> {
                testSnippetService.updateTestSnippet(id, request, userId)
            }
        }

        @Test
        fun `should throw if not authorized`() {
            val id = UUID.randomUUID().toString()
            val snippetId = UUID.randomUUID().toString()
            val userId = "user-1"
            val snippet = mockk<Snippet>(relaxed = true)
            val testSnippet = TestSnippet()
            val request = CreateTestSnippetRequest(id, snippetId, "new", mutableListOf("x"), mutableListOf("y"))
            every { testSnippetRepository.findById(UUID.fromString(id)) } returns Optional.of(testSnippet)
            every { snippetRepository.findById(UUID.fromString(snippetId)) } returns Optional.of(snippet)
            every { authApiClient.isUserAuthorizedToWriteSnippet(snippetId, userId) } returns false
            assertFailsWith<IllegalAccessException> {
                testSnippetService.updateTestSnippet(id, request, userId)
            }
        }
    }
}
