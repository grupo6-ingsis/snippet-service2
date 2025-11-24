package org.gudelker.snippet.service.modules.formatting

import org.gudelker.snippet.service.modules.formatting.formatconfig.input.FormatSingleSnippetRequest
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/format")
class FormatterController(private val formattingOrchestratorService: FormattingOrchestratorService) {
    private val logger = LoggerFactory.getLogger(FormatterController::class.java)

    @PostMapping("/snippet")
    fun formatSnippet(
        @RequestBody input: FormatSingleSnippetRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<String> {
        val id = UUID.fromString(input.snippetId)
        val result = formattingOrchestratorService.formatSingleSnippet(id, jwt.subject)
        logger.info("Successfully formatted snippet: {} for user: {}", input.snippetId, jwt.subject)
        return ResponseEntity.ok(result)
    }
}
