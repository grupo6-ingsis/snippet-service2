package org.gudelker.snippet.service.modules.interpret

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
    @PostMapping("/{snippetId}")
    fun interpretSnippet(
        @RequestBody input: InterpretSnippetRequest,
        @PathVariable snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): InterpretSnippetResponse {
        return interpretService.interpretSnippet(input, snippetId, jwt.subject)
    }
}
