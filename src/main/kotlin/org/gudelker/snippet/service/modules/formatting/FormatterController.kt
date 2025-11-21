package org.gudelker.snippet.service.modules.formatting

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/format")
class FormatterController(private val formattingOrchestratorService: FormattingOrchestratorService) {
    @PostMapping("/snippet/{snippetId}")
    fun formatSnippet(
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<String> {
        val id = UUID.fromString(snippetId)
        val result = formattingOrchestratorService.formatSingleSnippet(id, jwt.subject)
        return ResponseEntity.ok(result)
    }
}
