package org.gudelker.snippet.service

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
class SnippetServiceApplicationTests {
    @Test
    fun contextLoads() {
        print("Context Loads")
    }
}
