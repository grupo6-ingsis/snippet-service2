package org.gudelker.snippet.service.redis.producer

import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.RedisTemplate

abstract class RedisStreamProducer(
    private val streamKey: String,
    private val redis: RedisTemplate<String, String>,
) {
    protected fun emit(value: String): RecordId? {
        val record = MapRecord.create(streamKey, mapOf("data" to value))
        return redis
            .opsForStream<String, String>()
            .add(record)
    }
}
