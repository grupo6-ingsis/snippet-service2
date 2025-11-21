package modules.redis.unit

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gudelker.snippet.service.redis.dto.FormatRequest
import org.gudelker.snippet.service.redis.dto.LintRequest
import org.gudelker.snippet.service.redis.producer.FormatPublisher
import org.gudelker.snippet.service.redis.producer.LintPublisher
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.RedisTemplate
import kotlin.test.Test
import kotlin.test.assertSame

class RedisTests {
    @Test
    fun `FormatPublisher should serialize and emit`() {
        val redisTemplate = mockk<RedisTemplate<String, String>>(relaxed = true)
        val objectMapper = mockk<ObjectMapper>()
        val publisher = FormatPublisher(redisTemplate, objectMapper)
        val request = mockk<FormatRequest>()
        val json = "{\"foo\":1}"
        val recordId = mockk<RecordId>()
        every { objectMapper.writeValueAsString(request) } returns json
        every { redisTemplate.opsForStream<String, String>().add(any()) } returns recordId
        val result = publisher.publishFormatRequest(request)
        assertSame(recordId, result)
        verify { objectMapper.writeValueAsString(request) }
        verify { redisTemplate.opsForStream<String, String>().add(any()) }
    }

    @Test
    fun `LintPublisher should serialize and emit`() {
        val redisTemplate = mockk<RedisTemplate<String, String>>(relaxed = true)
        val objectMapper = mockk<ObjectMapper>()
        val publisher = LintPublisher(redisTemplate, objectMapper)
        val request = mockk<LintRequest>()
        val json = "{\"bar\":2}"
        val recordId = mockk<RecordId>()
        every { objectMapper.writeValueAsString(request) } returns json
        every { redisTemplate.opsForStream<String, String>().add(any()) } returns recordId
        val result = publisher.publishLintRequest(request)
        assertSame(recordId, result)
        verify { objectMapper.writeValueAsString(request) }
        verify { redisTemplate.opsForStream<String, String>().add(any()) }
    }
}
