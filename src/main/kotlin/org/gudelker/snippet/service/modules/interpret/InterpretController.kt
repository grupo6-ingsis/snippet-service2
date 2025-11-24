package org.gudelker.snippet.service.modules.interpret

import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/interpret")
class InterpretController(private val interpretService: InterpretService) {
    private val logger = LoggerFactory.getLogger(InterpretController::class.java)

    @PostMapping("/{snippetId}")
    fun interpretSnippet(
        @RequestBody input: InterpretSnippetRequest,
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): InterpretSnippetResponse {
        logger.info("User: {} requesting interpretation for snippet: {} with inputs: {}", jwt.subject, snippetId, input.inputs?.size ?: 0)
        val response = interpretService.interpretSnippet(input, snippetId, jwt.subject)
        logger.info("Successfully interpreted snippet: {} for user: {}", snippetId, jwt.subject)
        return response
    }
}
