package org.gudelker.snippet.service.redis.consumer

import org.gudelker.snippet.service.redis.dto.SnippetIdWithLintResultsDto
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.stream.StreamMessageListenerContainer
import java.time.Duration

@Configuration
class RedisLintResultsConfig {

    @Bean
    fun lintResultsListenerContainer(
        redisConnectionFactory: RedisConnectionFactory
    ): StreamMessageListenerContainer<String, ObjectRecord<String, SnippetIdWithLintResultsDto>> {

        val options = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
            .builder()
            .pollTimeout(Duration.ofMillis(100))
            .targetType(SnippetIdWithLintResultsDto::class.java)
            .build()

        return StreamMessageListenerContainer.create(redisConnectionFactory, options)
    }
}
