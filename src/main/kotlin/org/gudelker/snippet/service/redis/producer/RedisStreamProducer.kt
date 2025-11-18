package org.gudelker.snippet.service.redis.producer

import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.RedisTemplate

abstract class RedisStreamProducer(
    private val streamKey: String,
    private val redis: RedisTemplate<String, Any>,
) {
    protected fun emit(value: Any): RecordId? {
        val record = ObjectRecord.create(streamKey, value)
        return redis
            .opsForStream<String, Any>()
            .add(record)
    }
}
