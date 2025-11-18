package org.gudelker.snippet.service.redis.producer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {
    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().registerKotlinModule()
    }

    @Bean
    fun redisTemplate(factory: RedisConnectionFactory): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.connectionFactory = factory
        template.keySerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        template.hashValueSerializer = StringRedisSerializer()
        template.afterPropertiesSet()
        return template
    }
}
