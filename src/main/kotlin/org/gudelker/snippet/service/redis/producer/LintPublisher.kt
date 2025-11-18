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
        println("Publishing to Redis stream: $request")
        // Serializar el objeto a JSON string
        val jsonString = objectMapper.writeValueAsString(request)
        println("Serialized JSON: $jsonString")
        val recordId = emit(jsonString)
        println("Published with RecordId: $recordId")
        return recordId
    }
}
