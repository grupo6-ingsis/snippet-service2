package org.gudelker.snippet.service.redis.producer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.gudelker.snippet.service.redis.dto.LintRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {
    @Bean
    fun redisTemplate(factory: RedisConnectionFactory): RedisTemplate<String, LintRequest> {
        val objectMapper = ObjectMapper().registerKotlinModule()
        val template = RedisTemplate<String, LintRequest>()
        template.connectionFactory = factory
        template.keySerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.valueSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        template.hashValueSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        template.afterPropertiesSet()
        return template
    }
}
