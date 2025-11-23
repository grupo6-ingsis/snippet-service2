package org.gudelker.snippet.service.modules

import org.gudelker.snippet.service.SnippetServiceApplication
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [SnippetServiceApplication::class])
@ActiveProfiles("test")
class SnippetApplicationTests {
    @Test
    fun contextLoads() {
    }
}
