package org.gudelker.snippet.service.redis.consumer

import jakarta.annotation.PostConstruct
import org.gudelker.snippet.service.modules.snippets.SnippetService
import org.gudelker.snippet.service.redis.dto.SnippetIdWithLintResultsDto
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamListener
import org.springframework.data.redis.stream.StreamMessageListenerContainer
import org.springframework.stereotype.Service

@Service
class LintResultConsumer(
    private val snippetService: SnippetService,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val container: StreamMessageListenerContainer<String, ObjectRecord<String, SnippetIdWithLintResultsDto>>,
) : StreamListener<String, ObjectRecord<String, SnippetIdWithLintResultsDto>> {
    private val streamKey = "lint-results"
    private val group = "lint-results-group"
    private val consumerName = "snippet-service-1"

    @PostConstruct
    fun init() {
        try {
            redisTemplate
                .opsForStream<String, Any>()
                .createGroup(streamKey, group)
        } catch (e: Exception) {
            println("Group ya existe, OK")
        }

        container.receive(
            Consumer.from(group, consumerName),
            StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
            this,
        )

        container.start()
    }

    override fun onMessage(record: ObjectRecord<String, SnippetIdWithLintResultsDto>) {
        val result = record.value

        snippetService.updateLintResult(result.snippetId, result.results)

        redisTemplate
            .opsForStream<String, Any>()
            .acknowledge(streamKey, group, record.id)
    }
}
