package org.gudelker.snippet.service.redis

import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.RedisTemplate

abstract class RedisStreamProducer(
    private val streamKey: String,
    private val redis: RedisTemplate<String, Any>
) {

    protected fun emit(value: Any): RecordId? {
        val record = StreamRecords.newRecord()
            .ofObject(value)
            .withStreamKey(streamKey)

        return redis
            .opsForStream<String, Any>()
            .add(record)
    }
}
