package org.gudelker.snippet.service.redis.producer

import com.fasterxml.jackson.databind.ObjectMapper
import org.gudelker.snippet.service.redis.dto.FormatRequest
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class FormatPublisher(
    redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
) : RedisStreamProducer("formatting-requests", redisTemplate) {
    fun publishFormatRequest(request: FormatRequest): RecordId? {
        println("Publishing to Redis stream: $request")
        val jsonString = objectMapper.writeValueAsString(request)
        println("Serialized JSON: $jsonString")
        val recordId = emit(jsonString)
        println("Published with RecordId: $recordId")
        return recordId
    }
}
