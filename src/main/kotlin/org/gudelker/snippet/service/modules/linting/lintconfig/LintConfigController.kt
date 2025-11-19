package org.gudelker.snippet.service.modules.linting.lintconfig

import org.gudelker.snippet.service.modules.linting.lintconfig.input.ActivateRuleRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/lintconfig")
class LintConfigController(private val lintConfigService: LintConfigService) {
    @GetMapping("/user")
    fun getLintConfigsByUserId(
        @AuthenticationPrincipal jwt: Jwt,
    ): List<LintConfig> {
        return lintConfigService.getAllRulesFromUser(jwt.subject)
    }

    @PostMapping
    fun modifyRule(
        @RequestBody request: ActivateRuleRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<LintConfig> {
        val result = lintConfigService.modifyRule(request, jwt.subject)
        return if (result != null) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.noContent().build()
        }
    }
}
