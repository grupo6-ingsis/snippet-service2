package org.gudelker.snippet.service.redis.producer

import org.gudelker.snippet.service.redis.dto.LintRequest
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class LintPublisher(
    redisTemplate: RedisTemplate<String, LintRequest>,
) : RedisStreamProducer("lint-requests", redisTemplate) {
    fun publishLintRequest(request: LintRequest): RecordId? {
        println("Publishing to Redis stream: $request")
        val recordId = emit(request)
        println("Published with RecordId: $recordId")
        return recordId
    }
}
