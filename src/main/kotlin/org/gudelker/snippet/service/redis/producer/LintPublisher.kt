package org.gudelker.snippet.service.redis.producer

import com.fasterxml.jackson.databind.ObjectMapper
import org.gudelker.snippet.service.redis.dto.LintRequest
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class LintPublisher(
    redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
) : RedisStreamProducer("lint-requests", redisTemplate) {
    fun publishLintRequest(request: LintRequest): RecordId? {
        val jsonString = objectMapper.writeValueAsString(request)
        val recordId = emit(jsonString)
        return recordId
    }
}
