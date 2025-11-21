package org.gudelker.snippet.service.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/snippets/**").authenticated()
                    .requestMatchers("/lintconfig/**").authenticated()
                    .requestMatchers("/format/**").authenticated()
                    .requestMatchers("/testsnippet/**").authenticated()
                    .requestMatchers("/language/**").authenticated()
                    .requestMatchers("/users/**").authenticated()
                    .requestMatchers("/language-version/**").authenticated()
                    .requestMatchers("/formatconfig/**").authenticated()
                    .anyRequest().permitAll()
            }
            .oauth2ResourceServer { it.jwt {} }
        return http.build()
    }
}
