package org.gudelker.snippet.service.modules.lintconfig

import org.gudelker.snippet.service.modules.lintconfig.input.ActivateRuleRequest
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
    fun activateRule(
        @RequestBody request: ActivateRuleRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): LintConfig {
        return lintConfigService.activateRule(request, jwt.subject)
    }

    @DeleteMapping
    fun deactivateRule(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam lintRuleId: String,
    ) {
        lintConfigService.deactivateRule(jwt.subject, lintRuleId)
    }
}
